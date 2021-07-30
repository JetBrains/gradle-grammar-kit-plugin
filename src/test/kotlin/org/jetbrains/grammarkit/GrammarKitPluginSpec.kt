package org.jetbrains.grammarkit

import kotlin.test.Test
import kotlin.test.assertEquals

class GrammarKitPluginSpec : GrammarKitPluginBase() {

    @Test
    fun `grammarKit-specific tasks`() {
        assertEquals(
            listOf(
                GrammarKitConstants.GENERATE_LEXER_TASK_NAME,
                GrammarKitConstants.GENERATE_PARSER_TASK_NAME,
            ),
            tasks(GrammarKitConstants.GROUP_NAME),
        )
    }
}
