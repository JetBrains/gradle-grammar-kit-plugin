// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

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
