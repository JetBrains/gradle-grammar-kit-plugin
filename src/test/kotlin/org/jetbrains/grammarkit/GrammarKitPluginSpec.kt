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

    @Test
    fun `support centralized repository declaration`() {
        settingsFile.groovy("""
            dependencyResolutionManagement {
                repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
            
                repositories {
                    mavenCentral()
                }
            }
            
            rootProject.name = 'projectName'
        """.trimIndent())
        buildFile.writeText(
            buildFile.readText().replace(
                """
                repositories {
                    mavenCentral()
                }
                """.trimIndent(),
                ""
            )
        )

        assertEquals(
            listOf(
                GrammarKitConstants.GENERATE_LEXER_TASK_NAME,
                GrammarKitConstants.GENERATE_PARSER_TASK_NAME,
            ),
            tasks(GrammarKitConstants.GROUP_NAME),
        )
    }

}
