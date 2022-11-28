// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.plugins.*
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion
import org.jetbrains.grammarkit.GrammarKitConstants.MINIMAL_SUPPORTED_GRADLE_VERSION
import org.jetbrains.grammarkit.tasks.*
import java.io.*
import java.net.*

@Suppress("unused")
open class GrammarKitPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkGradleVersion()

        val extension = project.extensions.create<GrammarKitPluginExtension>(GrammarKitConstants.GROUP_NAME)

        val grammarKitClassPathConfiguration =
            project.configurations.register(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME)
        val compileClasspathConfiguration =
            project.configurations.named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
        val compileOnlyConfiguration = project.configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

        project.tasks.register<GenerateLexerTask>(GrammarKitConstants.GENERATE_LEXER_TASK_NAME)

        project.tasks.withType<GenerateLexerTask>().configureEach {
            classpath(
                getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                    file.name.startsWith("jflex")
                }
            )
        }

        project.tasks.register<GenerateParserTask>(GrammarKitConstants.GENERATE_PARSER_TASK_NAME)

        project.tasks.withType<GenerateParserTask>().configureEach {
            val requiredLibs = listOf(
                "app", "jdom", "trove4j", "junit", "guava", "asm-all", "automaton", "platform-api", "platform-impl",
                "util", "util_rt", "annotations", "picocontainer", "extensions", "idea", "openapi", "grammar-kit",
                "platform-util-ui", "platform-concurrency", "intellij-deps-fastutil",
                // CLion unlike IDEA contains `MockProjectEx` in `testFramework.jar` instead of `idea.jar`
                // so this jar should be in `requiredLibs` list to avoid `NoClassDefFoundError` exception
                // while parser generation with CLion distribution
                "testFramework", "3rd-party",
            )

            classpath(
                getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                    requiredLibs.any {
                        file.name.equals("$it.jar", true) || file.name.startsWith("$it-", true)
                    }
                }
            )
        }

        project.repositories.apply {
            maven {
                url = URI("https://cache-redirector.jetbrains.com/intellij-dependencies")
            }
            maven {
                url = URI("https://cache-redirector.jetbrains.com/intellij-repository/releases")
            }
        }

        project.afterEvaluate {
            val grammarKitRelease = extension.grammarKitRelease.get()
            val jflexRelease = extension.jflexRelease.get()
            val intellijRelease = extension.intellijRelease.orNull

            if (intellijRelease == null) {
                compileOnlyConfiguration.configure {
                    dependencies.addAll(
                        listOf(
                            "org.jetbrains:grammar-kit:$grammarKitRelease",
                            "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                        ).map(project.dependencies::create)
                    )

                    exclude(
                        mapOf(
                            "group" to "org.jetbrains.plugins",
                            "module" to "ant",
                        )
                    )
                    exclude(
                        mapOf(
                            "group" to "org.jetbrains.plugins",
                            "module" to "idea",
                        )
                    )
                }
            } else {
                configureGrammarKitClassPath(
                    project, grammarKitClassPathConfiguration, grammarKitRelease, jflexRelease, intellijRelease
                )
            }
        }
    }

    private fun configureGrammarKitClassPath(
        project: Project,
        grammarKitClassPathConfiguration: NamedDomainObjectProvider<Configuration>,
        grammarKitRelease: String,
        jflexRelease: String,
        intellijRelease: String,
    ) {
        grammarKitClassPathConfiguration.configure {
            dependencies.addAll(
                listOf(
                    "org.jetbrains:grammar-kit:$grammarKitRelease",
                    "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                    "com.jetbrains.intellij.platform:indexing-impl:$intellijRelease",
                    "com.jetbrains.intellij.platform:analysis-impl:$intellijRelease",
                    "com.jetbrains.intellij.platform:core-impl:$intellijRelease",
                    "com.jetbrains.intellij.platform:lang-impl:$intellijRelease",
                    "org.jetbrains.intellij.deps:asm-all:7.0.1",
                ).map(project.dependencies::create)
            )

            exclude(mapOf("group" to "com.jetbrains.rd"))
            exclude(mapOf("group" to "org.jetbrains.marketplace"))
            exclude(mapOf("group" to "org.roaringbitmap"))
            exclude(mapOf("group" to "org.jetbrains.plugins"))
            exclude(mapOf("module" to "idea"))
            exclude(mapOf("module" to "ant"))
        }
    }

    private fun getClasspath(
        grammarKitClassPathConfiguration: Provider<Configuration>,
        compileClasspathConfiguration: Provider<Configuration>,
        filter: (File) -> Boolean,
    ): Provider<Collection<File>> =
        grammarKitClassPathConfiguration.zip(compileClasspathConfiguration) { grammar, compile ->
            if (!grammar.isEmpty) {
                grammar.files
            } else compile.files.filter(filter)
        }

    private fun checkGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MINIMAL_SUPPORTED_GRADLE_VERSION)) {
            throw PluginInstantiationException("gradle-grammarkit-plugin requires Gradle $MINIMAL_SUPPORTED_GRADLE_VERSION and higher")
        }
    }
}
