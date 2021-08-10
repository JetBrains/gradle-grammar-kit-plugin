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
                source = "${getResourceFile("generateLexer/Example.flex")}"
                targetDir = "gen/org/jetbrains/grammarkit/lexer/"
                targetClass = "ExampleLexer"
            }
        """)

        val result = build(GrammarKitConstants.GENERATE_LEXER_TASK_NAME)

        assertTrue(result.output.contains("> Task :${GrammarKitConstants.GENERATE_LEXER_TASK_NAME}"))
        assertTrue(result.output.contains("Writing code to \"${dir.canonicalPath}/gen/org/jetbrains/grammarkit/lexer/GeneratedLexer.java\""))
    }

//    @Test
//    fun `do not fail on warning by default`() {
//        buildFile.groovy("""
//            generateLexer {
//                source = "${getResourceFile("Example.flex")}"
//                targetDir = "gen/org/jetbrains/grammarkit/lexer/"
//                targetClass = "ExampleLexer"
//                skeleton = "${getResourceFile("idea-flex.skeleton")}"
//                purgeOldFiles = true
//            }
//        """)
//
//        val result = build(GrammarKitConstants.GENERATE_LEXER_TASK_NAME)
//
//        assertTrue(result.output.contains("> Task :${GrammarKitConstants.GENERATE_LEXER_TASK_NAME}"))
//    }
}
