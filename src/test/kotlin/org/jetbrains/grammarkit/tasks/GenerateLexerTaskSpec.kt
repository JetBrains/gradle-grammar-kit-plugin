// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.intellij.lang.annotations.Language
import org.jetbrains.grammarkit.GrammarKitConstants.GENERATE_LEXER_TASK_NAME
import org.jetbrains.grammarkit.GrammarKitPluginBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateLexerTaskSpec : GrammarKitPluginBase() {

    @Test
    fun `run lexer`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
            }
        """)

        val result = build(GENERATE_LEXER_TASK_NAME)

        assertTrue(result.output.contains("> Task :$GENERATE_LEXER_TASK_NAME"))
        assertTrue(adjustWindowsPath(result.output).contains("Writing code to \"${adjustWindowsPath(dir.canonicalPath)}/gen/GeneratedLexer.java\""))
    }

    @Test
    fun `reuse configuration cache`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
            }
        """)

        val firstRun = build(GENERATE_LEXER_TASK_NAME, "--configuration-cache")
        assertEquals(SUCCESS, firstRun.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
        val secondRun = build(GENERATE_LEXER_TASK_NAME, "--configuration-cache")

        assertTrue(secondRun.output.contains("Reusing configuration cache."))
        assertEquals(UP_TO_DATE, secondRun.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
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
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetRootOutputDir = project.layout.buildDirectory.dir("gen/")
            }
        """)

        val firstRun = build(GENERATE_LEXER_TASK_NAME, "--build-cache")
        assertEquals(SUCCESS, firstRun.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)

        build("clean", "--build-cache")

        val secondRun = build(GENERATE_LEXER_TASK_NAME, "--build-cache")
        assertEquals(TaskOutcome.FROM_CACHE, secondRun.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
    }

    @Test
    fun `support java srcDir`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
            }
            sourceSets.main {
              java.srcDir(tasks.generateLexer)
            }
            // Skip compilation as it would fail since the necissary dependnecies are not available
            tasks.compileJava.onlyIf { false }
        """)

        val result = build("compileJava")

        assertEquals(SUCCESS, result.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
    }

    @Test
    fun `support package detection`() {
        testPackage(
            "some/pkg/",
            preamble = "package some.pkg;",
            configuration = """
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
                """.trimIndent(),
        )
    }

    @Test
    fun `support unnamed package`() {
        testPackage(
            "",
            preamble = "//package some.pkg;",
            configuration = """
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
                """.trimIndent(),
            failedDetection = true,
        )
    }

    @Test
    fun `ignore package in targetOutputDir`() {
        testPackage(
            "",
            preamble = "package some.pkg;",
            configuration = """
                targetOutputDir = project.layout.projectDirectory.dir("gen/")
                """.trimIndent(),
        )
    }

    @Test
    fun `support explicit package`() {
        testPackage(
            "some/other/pkg/",
            configuration = """
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
                packageName = "some.other.pkg"
                """.trimIndent(),
        )
    }

    @Test
    fun `support explicit unnamed package`() {
        testPackage(
            "",
            configuration = """
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
                packageName = ""
                """.trimIndent(),
        )
    }

    private fun testPackage(
        subdirPrefix: String,
        failedDetection: Boolean = false,
        @Language("Java") preamble: String? = null,
        @Language("Groovy", prefix = "//file:noinspection GroovyUnusedAssignment\n") configuration: String = "",
    ) {
        val lexerSource = preamble?.let {
            val sourceFile = file("ModifiableExample.flex")
            sourceFile.java(it)
            sourceFile.appendText(getResourceContent("generateLexer/Example.flex"))
            "ModifiableExample.flex"
        } ?: getResourceFile("generateLexer/Example.flex")

        buildFile.groovy("""
            |generateLexer {
            |    sourceFile = project.file("$lexerSource")
            |    ${configuration.trimIndent().replace("\n", "\n|    ")}
            |}
        """.trimMargin())

        val result = build(GENERATE_LEXER_TASK_NAME)
        assertEquals(SUCCESS, result.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
        assertTrue("GeneratedLexer.java doesn't exist in unnamed package") { dir.resolve("gen/${subdirPrefix}GeneratedLexer.java").exists() }
        assertEquals(failedDetection, result.output.contains("Could not detect `packageName`"), "detection failed")
    }

    @Test
    fun `purge stale files by default`() {
        testPurgeStaleFiles(
            expectPurge = true,
            configuration = """
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
                """.trimIndent(),
        )
    }

    @Test
    fun `do not purge stale files when disabled`() {
        testPurgeStaleFiles(
            expectPurge = false,
            configuration = """
                targetRootOutputDir = project.layout.projectDirectory.dir("gen/")
                purgeOldFiles = false
                """.trimIndent(),
        )
    }

    @Test
    fun `do not purge stale files by default when using targetOutputDir`() {
        testPurgeStaleFiles(
            expectPurge = false,
            configuration = """
                targetOutputDir = project.layout.projectDirectory.dir("gen/")
                """.trimIndent(),
        )
    }

    private fun testPurgeStaleFiles(
        expectPurge: Boolean,
        @Language("Groovy", prefix = "//file:noinspection GroovyUnusedAssignment\n") configuration: String,
    ) {
        val sourceFile = file("ModifiableExample.flex")
        sourceFile.appendText(getResourceContent("generateLexer/Example.flex"))

        buildFile.groovy("""
            |generateLexer {
            |    sourceFile = project.file("ModifiableExample.flex")
            |    ${configuration.trimIndent().replace("\n", "\n|    ")}
            |}
        """.trimMargin())

        val firstRun = build(GENERATE_LEXER_TASK_NAME)
        val markerFile = file("gen/STALE.txt")

        assertEquals(SUCCESS, firstRun.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
        assertTrue("marker file doesn't exist") { markerFile.exists() }
        assertTrue("GeneratedLexer.java doesn't exist") { dir.resolve("gen/GeneratedLexer.java").exists() }

        sourceFile.writeText(sourceFile.readText().replace("GeneratedLexer", "GeneratedLexer2"))
        val secondRun = build(GENERATE_LEXER_TASK_NAME)

        assertEquals(SUCCESS, secondRun.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
        assertTrue("GeneratedLexer2.java doesn't exist") { dir.resolve("gen/GeneratedLexer2.java").exists() }

        assertEquals(expectPurge, !markerFile.exists(), "marker file purged")
        assertEquals(expectPurge, !dir.resolve("gen/GeneratedLexer.java").exists(), "GeneratedLexer.java purged")
    }

    @Test
    fun `report error if no output directory is specified`() {
        buildFile.groovy("""
            import org.jetbrains.grammarkit.tasks.GenerateLexerTask
            task lexerTask(type: GenerateLexerTask) {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
            }
        """.trimIndent())

        val result = build(fail = true, "lexerTask")

        assertEquals(FAILED, result.task(":lexerTask")?.outcome)
        assertTrue("targetRootOutputDir not mentioned") { result.output.contains("targetRootOutputDir") }
        assertTrue("targetOutputDir not mentioned") { result.output.contains("targetOutputDir") }
    }

    @Test
    fun `provide default for targetRootOutputDir of generateLexer task`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                // do not specify targetRootOutputDir
            }
        """.trimIndent())

        val result = build(GENERATE_LEXER_TASK_NAME)

        assertEquals(SUCCESS, result.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
    }
}
