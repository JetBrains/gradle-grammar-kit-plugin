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
        assertTrue(result.output.contains("Example.bnf parser generated to ${adjustWindowsPath(dir.canonicalPath)}/gen"))
    }

    @Test
    fun `reuse configuration cache`() {
        buildFile.groovy("""
            generateParser {
                source = "${getResourceFile("generateParser/Example.bnf")}"
                targetRoot = "gen"
                pathToParser = "/org/jetbrains/grammarkit/IgnoreParser.java"
                pathToPsiRoot = "/org/jetbrains/grammarkit/psi"
            }
        """)

        build(GrammarKitConstants.GENERATE_PARSER_TASK_NAME, "--configuration-cache")
        val result = build(GrammarKitConstants.GENERATE_PARSER_TASK_NAME, "--configuration-cache")

        assertTrue(result.output.contains("Reusing configuration cache."))
    }
}
