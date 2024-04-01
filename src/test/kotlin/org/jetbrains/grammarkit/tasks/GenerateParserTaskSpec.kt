// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.gradle.testkit.runner.TaskOutcome.*
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
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
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
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
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
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
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
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
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
}
