package org.jetbrains.grammarkit.tasks

import org.jetbrains.grammarkit.GrammarKitConstants
import org.jetbrains.grammarkit.GrammarKitPluginBase
import kotlin.test.assertTrue
import kotlin.test.Test

class GenerateParserTaskSpec : GrammarKitPluginBase() {

    @Test
    fun `run parser with a custom task name`() {
        val taskName = "generateMyParser"
        buildFile.groovy("""
            import org.jetbrains.grammarkit.tasks.GenerateParserTask
            
            task $taskName(type: GenerateParserTask) {
                source = "${getResourceFile("generateParser/Example.bnf")}"
                targetRoot = "gen"
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
            }
        """)

        val result = build(taskName)

        assertTrue(result.output.contains("> Task :$taskName"))
        assertTrue(result.output.contains("Writing code to \"${dir.canonicalPath}/gen/org/jetbrains/grammarkit/lexer/GeneratedLexer.java\""))
    }

    @Test
    fun `run parser`() {
        buildFile.groovy("""
            generateParser {
                source = "${getResourceFile("generateLexer/Example.flex")}"
                targetDir = "gen/org/jetbrains/grammarkit/lexer/"
                targetClass = "ExampleLexer"
            }
        """)

        val result = build(GrammarKitConstants.GENERATE_PARSER_TASK_NAME)

        assertTrue(result.output.contains("> Task :${GrammarKitConstants.GENERATE_PARSER_TASK_NAME}"))
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
