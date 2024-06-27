// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun `main source set does not depend on tasks by default`() {
        val result = build("compileJava")

        assertEquals(TaskOutcome.NO_SOURCE, result.task(":compileJava")?.outcome)
        assertNull(result.task(":${GrammarKitConstants.GENERATE_LEXER_TASK_NAME}")?.outcome)
        assertNull(result.task(":${GrammarKitConstants.GENERATE_PARSER_TASK_NAME}")?.outcome)
    }

    @Test
    fun `main source set depends on generateLexer when using lexerSource`() {
        buildFile.groovy("""
            grammarKit {
                lexerSource = layout.projectDirectory.file("${getResourceFile("generateLexer/Example.flex")}")
            }
            // Skip compilation as it would fail since the necissary dependnecies are not available
            tasks.compileJava.onlyIf { false }
        """.trimIndent())

        val result = build("compileJava")

        assertEquals(TaskOutcome.SKIPPED, result.task(":compileJava")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":${GrammarKitConstants.GENERATE_LEXER_TASK_NAME}")?.outcome)
    }

    @Test
    fun `main source set depends on generateParser when using parserSource`() {
        buildFile.groovy("""
            grammarKit {
                parserSource = layout.projectDirectory.file("${getResourceFile("generateParser/Example.bnf")}")
            }
            // Skip compilation as it would fail since the necissary dependnecies are not available
            tasks.compileJava.onlyIf { false }
        """.trimIndent())

        val result = build("compileJava")

        assertEquals(TaskOutcome.SKIPPED, result.task(":compileJava")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":${GrammarKitConstants.GENERATE_PARSER_TASK_NAME}")?.outcome)
    }

}
