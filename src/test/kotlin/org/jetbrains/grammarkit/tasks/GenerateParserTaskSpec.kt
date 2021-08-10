package org.jetbrains.grammarkit.tasks

import org.jetbrains.grammarkit.GrammarKitConstants
import org.jetbrains.grammarkit.GrammarKitPluginBase
import kotlin.test.Test
import kotlin.test.assertTrue

class GenerateParserTaskSpec : GrammarKitPluginBase() {

    @Test
    fun `run parser`() {
        buildFile.groovy("""
            generateParser {
                source = "${getResourceFile("generateParser/Example.bnf")}"
                targetRoot = "gen"
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
            }
        """)

        val result = build(GrammarKitConstants.GENERATE_PARSER_TASK_NAME)

        assertTrue(result.output.contains("> Task :${GrammarKitConstants.GENERATE_PARSER_TASK_NAME}"))
        assertTrue(result.output.contains("Example.bnf parser generated to ${dir.canonicalPath}/gen"))
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
