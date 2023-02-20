// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS
import org.gradle.api.plugins.JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion
import org.jetbrains.grammarkit.GrammarKitConstants.GENERATE_LEXER_TASK_NAME
import org.jetbrains.grammarkit.GrammarKitConstants.GENERATE_PARSER_TASK_NAME
import org.jetbrains.grammarkit.GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME
import org.jetbrains.grammarkit.GrammarKitConstants.MINIMAL_SUPPORTED_GRADLE_VERSION
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import java.io.File

@Suppress("UnstableApiUsage")
abstract class GrammarKitPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkGradleVersion()

        val extension = project.extensions.create<GrammarKitPluginExtension>(GrammarKitConstants.GROUP_NAME)

        val grammarKitClassPathConfiguration = project.configurations.register(GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME)
        val compileClasspathConfiguration = project.configurations.named(COMPILE_CLASSPATH_CONFIGURATION_NAME)
        val compileOnlyConfiguration = project.configurations.named(COMPILE_ONLY_CONFIGURATION_NAME)

        project.tasks.register<GenerateLexerTask>(GENERATE_LEXER_TASK_NAME)

        project.tasks.withType<GenerateLexerTask>().configureEach {
            classpath(getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                file.name.startsWith("jflex")
            })
        }

        project.tasks.register<GenerateParserTask>(GENERATE_PARSER_TASK_NAME)

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

            classpath(getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                requiredLibs.any {
                    file.name.equals("$it.jar", true) || file.name.startsWith("$it-", true)
                }
            })
        }

        if (project.settings.dependencyResolutionManagement.repositoriesMode.get() != FAIL_ON_PROJECT_REPOS) {
            project.repositories.apply {
                maven(url = "https://cache-redirector.jetbrains.com/intellij-dependencies")
                maven(url = "https://cache-redirector.jetbrains.com/intellij-repository/releases")
            }
        }

        compileOnlyConfiguration.configure {
            val grammarJFlexDependencies = zip(
                extension.grammarKitRelease,
                extension.jflexRelease,
            ) { grammarKitRelease, jflexRelease ->
                listOf(
                    "org.jetbrains:grammar-kit:$grammarKitRelease",
                    "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                ).map(project.dependencies::create).map {
                    it as ModuleDependency
                    it.exclude(mapOf("group" to "org.jetbrains.plugins", "module" to "ant"))
                    it.exclude(mapOf("group" to "org.jetbrains.plugins", "module" to "idea"))
                }
            }

            dependencies.addAllLater(zip(
                extension.intellijRelease,
                grammarJFlexDependencies,
            ) { intellijRelease, dependencies ->
                when {
                    intellijRelease.isEmpty() -> dependencies
                    else -> emptyList()
                }
            })
        }

        grammarKitClassPathConfiguration.configure {
            val platformDependencies = zip(
                extension.intellijRelease,
                extension.grammarKitRelease,
                extension.jflexRelease,
            ) { intellijRelease, grammarKitRelease, jflexRelease ->
                listOf(
                    "org.jetbrains:grammar-kit:$grammarKitRelease",
                    "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                    "com.jetbrains.intellij.platform:indexing-impl:$intellijRelease",
                    "com.jetbrains.intellij.platform:analysis-impl:$intellijRelease",
                    "com.jetbrains.intellij.platform:core-impl:$intellijRelease",
                    "com.jetbrains.intellij.platform:lang-impl:$intellijRelease",
                    "org.jetbrains.intellij.deps:asm-all:7.0.1",
                ).map(project.dependencies::create).map {
                    it as ModuleDependency
                    it.exclude(mapOf("group" to "com.jetbrains.rd"))
                    it.exclude(mapOf("group" to "org.jetbrains.marketplace"))
                    it.exclude(mapOf("group" to "org.roaringbitmap"))
                    it.exclude(mapOf("group" to "org.jetbrains.plugins"))
                    it.exclude(mapOf("module" to "idea"))
                    it.exclude(mapOf("module" to "ant"))
                }
            }
            dependencies.addAllLater(zip(
                extension.intellijRelease,
                platformDependencies,
            ) { intellijRelease, dependencies ->
                when {
                    intellijRelease.isEmpty() -> emptyList()
                    else -> dependencies
                }
            })
        }
    }

    private fun getClasspath(
        grammarKitClassPathConfiguration: Provider<Configuration>,
        compileClasspathConfiguration: Provider<Configuration>,
        filter: (File) -> Boolean,
    ): Provider<Collection<File>> = zip(grammarKitClassPathConfiguration, compileClasspathConfiguration) { grammar, compile ->
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
