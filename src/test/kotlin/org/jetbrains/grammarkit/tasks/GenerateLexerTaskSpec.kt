// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.jetbrains.grammarkit.GrammarKitConstants
import org.jetbrains.grammarkit.GrammarKitPluginBase
import kotlin.test.Test
import kotlin.test.assertTrue

class GenerateLexerTaskSpec : GrammarKitPluginBase() {

    @Test
    fun `run lexer`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetDir = "gen/org/jetbrains/grammarkit/lexer/"
                targetClass = "ExampleLexer"
            }
        """)

        val result = build(GrammarKitConstants.GENERATE_LEXER_TASK_NAME)

        assertTrue(result.output.contains("> Task :${GrammarKitConstants.GENERATE_LEXER_TASK_NAME}"))
        assertTrue(adjustWindowsPath(result.output).contains("Writing code to \"${adjustWindowsPath(dir.canonicalPath)}/gen/org/jetbrains/grammarkit/lexer/GeneratedLexer.java\""))
    }

    @Test
    fun `reuse configuration cache`() {
        buildFile.groovy("""
            generateLexer {
                sourceFile = project.file("${getResourceFile("generateLexer/Example.flex")}")
                targetDir = "gen/org/jetbrains/grammarkit/lexer/"
                targetClass = "ExampleLexer"
            }
        """)

        build(GrammarKitConstants.GENERATE_LEXER_TASK_NAME, "--configuration-cache")
        val result = build(GrammarKitConstants.GENERATE_LEXER_TASK_NAME, "--configuration-cache")

        assertTrue(result.output.contains("Reusing configuration cache."))
    }
}
