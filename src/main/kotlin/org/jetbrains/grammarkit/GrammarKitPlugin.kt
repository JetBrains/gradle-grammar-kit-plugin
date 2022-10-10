// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginInstantiationException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.grammarkit.GrammarKitConstants.MINIMAL_SUPPORTED_GRADLE_VERSION
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import java.io.File
import java.net.URI

@Suppress("unused")
open class GrammarKitPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkGradleVersion(project)

        val extension = project.extensions.create<GrammarKitPluginExtension>(GrammarKitConstants.GROUP_NAME).apply {
            grammarKitRelease.convention(GrammarKitConstants.GRAMMAR_KIT_DEFAULT_VERSION)
            jflexRelease.convention(GrammarKitConstants.JFLEX_DEFAULT_VERSION)
        }

        val grammarKitClassPathConfiguration = project.configurations.create(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME)
        val compileClasspathConfiguration = project.configurations.maybeCreate(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
        val compileOnlyConfiguration = project.configurations.maybeCreate(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

        project.tasks.register<GenerateLexerTask>(GrammarKitConstants.GENERATE_LEXER_TASK_NAME) {
            description = "Generates lexers for IntelliJ-based plugin"
            group = GrammarKitConstants.GROUP_NAME
        }

        project.tasks.withType<GenerateLexerTask>().configureEach {
            targetOutputDir.convention(targetDir.map {
                project.layout.projectDirectory.dir(it)
            })
            targetFile.convention(targetClass.map {
                project.layout.projectDirectory.file("${targetDir.get()}/$it.java")
            })
            sourceFile.convention(source.map {
                project.layout.projectDirectory.file(it)
            })
            jFlexClasspath.setFrom(project.provider {
                getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                    file.name.startsWith("jflex")
                }
            })

            doFirst {
                if (purgeOldFiles.orNull == true) {
                    targetFile.get().asFile.deleteRecursively()
                }
            }
        }

        project.tasks.register<GenerateParserTask>(GrammarKitConstants.GENERATE_PARSER_TASK_NAME) {
            description = "Generates parsers for IntelliJ-based plugin"
            group = GrammarKitConstants.GROUP_NAME

            sourceFile.convention(source.map {
                project.layout.projectDirectory.file(it)
            })
            targetRootOutputDir.convention(targetRoot.map {
                project.layout.projectDirectory.dir(it)
            })
            parserFile.convention(pathToParser.map {
                project.layout.projectDirectory.file("${targetRoot.get()}/$it")
            })
            psiDir.convention(pathToPsiRoot.map {
                project.layout.projectDirectory.dir("${targetRoot.get()}/$it")
            })
        }

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

            grammarKitClasspath.setFrom(project.provider {
                getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                    requiredLibs.any {
                        file.name.equals("$it.jar", true) || file.name.startsWith("$it-", true)
                    }
                }
            })

            doFirst {
                if (purgeOldFiles.orNull == true) {
                    targetRootOutputDir.get().asFile.apply {
                        resolve(pathToParser.get()).deleteRecursively()
                        resolve(pathToPsiRoot.get()).deleteRecursively()
                    }
                }
            }
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
                compileOnlyConfiguration.apply {
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
        grammarKitClassPathConfiguration: Configuration,
        grammarKitRelease: String,
        jflexRelease: String,
        intellijRelease: String,
    ) {
        grammarKitClassPathConfiguration.apply {
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
        grammarKitClassPathConfiguration: Configuration,
        compileClasspathConfiguration: Configuration,
        filter: (File) -> Boolean,
    ) = when {
        !grammarKitClassPathConfiguration.isEmpty -> grammarKitClassPathConfiguration.files
        else -> compileClasspathConfiguration.files.filter(filter)
    }

    private fun checkGradleVersion(project: Project) {
        if (Version.parse(project.gradle.gradleVersion) < Version.parse(MINIMAL_SUPPORTED_GRADLE_VERSION)) {
            throw PluginInstantiationException("gradle-grammarkit-plugin requires Gradle $MINIMAL_SUPPORTED_GRADLE_VERSION and higher")
        }
    }
}
