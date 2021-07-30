package org.jetbrains.grammarkit

import kotlin.test.Test
import kotlin.test.assertEquals

class GrammarKitPluginSpec : GrammarKitPluginBase() {

    @Test
    fun `grammarKit-specific tasks`() {
        assertEquals(
            listOf(
            ),
            tasks(GrammarKitConstants.GROUP_NAME),
        )
    }
}
