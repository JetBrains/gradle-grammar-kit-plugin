// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.gradle.testkit.runner.TaskOutcome.*
import org.intellij.lang.annotations.Language
import org.jetbrains.grammarkit.GrammarKitConstants.GENERATE_PARSER_TASK_NAME
import org.jetbrains.grammarkit.GrammarKitPluginBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateParserTaskSpec : GrammarKitPluginBase() {

    @Test
    fun `run parser`() {
        buildFile.groovy("""
            generateParser {
                sourceFile = project.file("${getResourceFile("generateParser/Example.bnf")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen")
            }
        """)

        val result = build(GENERATE_PARSER_TASK_NAME)

        assertTrue(result.output.contains("> Task :$GENERATE_PARSER_TASK_NAME"))
        assertTrue(adjustWindowsPath(result.output).contains("Example.bnf parser generated to ${adjustWindowsPath(dir.canonicalPath)}/gen"))
    }

    @Test
    fun `reuse configuration cache`() {
        buildFile.groovy("""
            generateParser {
                sourceFile = project.file("${getResourceFile("generateParser/Example.bnf")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen")
            }
        """)

        val firstRun = build(GENERATE_PARSER_TASK_NAME, "--configuration-cache")
        assertEquals(SUCCESS, firstRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)

        val secondRun = build(GENERATE_PARSER_TASK_NAME, "--configuration-cache")
        assertTrue(secondRun.output.contains("Reusing configuration cache."))
        assertEquals(UP_TO_DATE, secondRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
    }

    @Test
    fun `supports build cache`() {
        settingsFile.groovy("""
            // Use temporary directory for build cache,
            // as the second execution of this test will fail otherwise if the cache hasn't been cleaned
            buildCache {
                local {
                    directory = file("build-cache")
                }
            }
        """.trimIndent())
        buildFile.groovy("""
            generateParser {
                sourceFile = project.file("${getResourceFile("generateParser/Example.bnf")}")
                targetRootOutputDir = project.layout.buildDirectory.dir("gen")
            }
        """)

        val firstRun = build(GENERATE_PARSER_TASK_NAME, "--build-cache")
        assertEquals(SUCCESS, firstRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)

        build("clean", "--build-cache")

        val secondRun = build(GENERATE_PARSER_TASK_NAME, "--build-cache")
        assertEquals(FROM_CACHE, secondRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
    }

    @Test
    fun `support java srcDir`() {
        buildFile.groovy("""
            generateParser {
                sourceFile = project.file("${getResourceFile("generateParser/Example.bnf")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen")
            }
            sourceSets.main {
              java.srcDir(tasks.generateParser)
            }
            // Skip compilation as it would fail since the necissary dependnecies are not available
            tasks.compileJava.onlyIf { false }
        """)

        val result = build("compileJava")

        assertEquals(SUCCESS, result.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
    }

    @Test
    fun `purge stale files when enabled`() {
        testPurgeStaleFiles(expectPurge = true, configuration = "purgeOldFiles = true")
    }

    @Test
    fun `purge stale files only in subdirectories when using pathToParser and pathToPsiRoot`() {
        testPurgeStaleFiles(
            expectPurgeUnrelatedFiles = false,
            expectPurgeParser = true,
            expectPurgePsiRoot = true,
            configuration = """
                pathToParser = "pkg/lang"
                pathToPsiRoot = "pkg/psi"
                purgeOldFiles = true
                """.trimIndent(),
        )
    }

    private fun testPurgeStaleFiles(
        expectPurge: Boolean? = null,
        expectPurgeUnrelatedFiles: Boolean = expectPurge!!,
        expectPurgeParser: Boolean = expectPurge!!,
        expectPurgePsiRoot: Boolean = expectPurge!!,
        @Language("Groovy", prefix = "//file:noinspection GroovyUnusedAssignment\n") configuration: String = "",
    ) {
        val sourceFile = file("ModifiableExample.bnf")
        sourceFile.bnf("""
            {
                parserClass="pkg.lang.MyParser1"
                psiPackage="pkg.psi"
                psiImplPackage="pkg.psi.impl"
            }
        """.trimIndent())
        sourceFile.appendText(getResourceContent("generateParser/Example.bnf"))
        sourceFile.bnf("\nmy_rule_1 ::=")
        buildFile.groovy("""
            |generateParser {
            |    sourceFile = project.file("ModifiableExample.bnf")
            |    targetRootOutputDir = project.layout.projectDirectory.dir("gen")
            |    ${configuration.trimIndent().replace("\n", "\n|    ")}
            |}
        """.trimMargin())

        val firstRun = build(GENERATE_PARSER_TASK_NAME)
        val markerFile = file("gen/pkg/STALE.txt")

        assertEquals(SUCCESS, firstRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
        assertTrue("marker file doesn't exist") { markerFile.exists() }
        assertTrue("MyParser1.java doesn't exist") { dir.resolve("gen/pkg/lang/MyParser1.java").exists() }
        assertTrue("MyRule1.java doesn't exist") { dir.resolve("gen/pkg/psi/MyRule1.java").exists() }

        sourceFile.writeText(sourceFile.readText().replace("MyParser1", "MyParser2"))
        sourceFile.writeText(sourceFile.readText().replace("my_rule_1", "my_rule_2"))
        val secondRun = build(GENERATE_PARSER_TASK_NAME)

        assertEquals(SUCCESS, secondRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
        assertTrue("MyParser2.java doesn't exist") { dir.resolve("gen/pkg/lang/MyParser2.java").exists() }
        assertTrue("MyRule2.java doesn't exist") { dir.resolve("gen/pkg/psi/MyRule2.java").exists() }

        assertEquals(expectPurgeUnrelatedFiles, !markerFile.exists(), "marker file purged")
        assertEquals(expectPurgeParser, !dir.resolve("gen/pkg/lang/MyParser1.java").exists(), "MyParser1.java purged")
        assertEquals(expectPurgePsiRoot, !dir.resolve("gen/pkg/psi/MyRule1.java").exists(), "MyRule1.java purged")
    }

    @Test
    fun `report error if only one of the properties pathToParser and pathToPsiRoot is configured`() {
        buildFile.groovy("""
            generateParser {
                sourceFile = project.file("${getResourceFile("generateParser/Example.bnf")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen")
                pathToParser = "pkg/lang"
                pathToPsiRoot = "pkg/psi"
            }
        """.trimIndent())

        buildFile.writeText(buildFile.readText().replace("pathToParser", "//pathToParser"))
        val firstRun = build(fail = true, GENERATE_PARSER_TASK_NAME)

        assertEquals(FAILED, firstRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
        assertTrue("error message missing") { firstRun.output.contains("'pathToPsiRoot' has a configured value, but 'pathToParser' has not.") }

        buildFile.writeText(buildFile.readText().replace("//pathToParser", "pathToParser"))
        buildFile.writeText(buildFile.readText().replace("pathToPsiRoot", "//pathToPsiRoot"))
        val secondRun = build(fail = true, GENERATE_PARSER_TASK_NAME)

        assertEquals(FAILED, secondRun.task(":$GENERATE_PARSER_TASK_NAME")?.outcome)
        assertTrue("error message missing") { secondRun.output.contains("'pathToParser' has a configured value, but 'pathToPsiRoot' has not.") }
    }
}
