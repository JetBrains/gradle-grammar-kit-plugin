// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
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
                targetOutputDir = project.layout.projectDirectory.dir("gen/org/jetbrains/grammarkit/lexer/")
            }
        """)

        val result = build(GENERATE_LEXER_TASK_NAME)

        assertTrue(result.output.contains("> Task :$GENERATE_LEXER_TASK_NAME"))
        assertTrue(adjustWindowsPath(result.output).contains("Writing code to \"${adjustWindowsPath(dir.canonicalPath)}/gen/org/jetbrains/grammarkit/lexer/GeneratedLexer.java\""))
    }

    @Test
    fun `reuse configuration cache`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetOutputDir = project.layout.projectDirectory.dir("gen/org/jetbrains/grammarkit/lexer/")
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
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetOutputDir = project.layout.buildDirectory.dir("gen/org/jetbrains/grammarkit/lexer/")
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
                targetOutputDir = project.layout.projectDirectory.dir("gen/org/jetbrains/grammarkit/lexer/")
            }
            sourceSets.main {
              java.srcDir(tasks.generateLexer)
            }
        """)

        val cantCompileJava = true
        val result = build(fail = cantCompileJava, "compileJava")

        assertEquals(SUCCESS, result.task(":$GENERATE_LEXER_TASK_NAME")?.outcome)
    }
}
