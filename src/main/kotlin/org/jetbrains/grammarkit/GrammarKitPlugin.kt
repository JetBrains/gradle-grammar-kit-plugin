package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import java.net.URI

@Suppress("unused")
open class GrammarKitPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            GrammarKitConstants.GROUP_NAME,
            GrammarKitPluginExtension::class.java,
        )

        extension.grammarKitRelease.set("2020.3.1")
        extension.jflexRelease.set("1.7.0-1")

        project.tasks.register(GrammarKitConstants.GENERATE_LEXER_TASK_NAME, GenerateLexerTask::class.java) { task ->
            task.description = "Generates lexers for IntelliJ-based plugin"
            task.group = GrammarKitConstants.GROUP_NAME

            task.targetFile.convention {
                project.file("${task.targetDir}/${task.targetClass}.java")
            }

            task.doFirst {
                if (task.purgeOldFiles.get()) {
                    project.delete(task.targetFile.get())
                }
            }
        }

        project.tasks.register(GrammarKitConstants.GENERATE_PARSER_TASK_NAME, GenerateParserTask::class.java) { task ->
            task.description = "Generates parsers for IntelliJ-based plugin"
            task.group = GrammarKitConstants.GROUP_NAME

            task.parserFile.convention {
                project.file("$task.targetRoot/$task.pathToParser")
            }
            task.psiDir.convention(
                project.layout.dir(project.provider {
                    project.file("${task.targetRoot}/${task.pathToPsiRoot}")
                })
            )

            task.doFirst {
                if (task.purgeOldFiles.get()) {
                    val parserFile = project.file("${task.targetRoot}/${task.pathToParser}")
                    val psiDir = project.file("${task.targetRoot}/${task.pathToPsiRoot}")

                    project.delete(parserFile)
                    project.delete(psiDir)
                }
            }
        }

        project.repositories.apply {
            maven {
                it.url = URI("https://cache-redirector.jetbrains.com/intellij-dependencies")
            }
            maven {
                it.url = URI("https://www.jetbrains.com/intellij-repository/releases")
            }
        }

        project.afterEvaluate {
            if (extension.intellijRelease.orNull == null) {
                project.dependencies.add(
                    JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                    "com.github.JetBrains:Grammar-Kit:${extension.grammarKitRelease.get()}",
                )
                project.dependencies.add(
                    JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                    "org.jetbrains.intellij.deps.jflex:jflex:${extension.jflexRelease.get()}",
                )
                project.dependencies.add(
                    JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                    "org.jetbrains.intellij.deps.jflex:jflex:${extension.jflexRelease.get()}",
                )
                project.dependencies.add(
                    GrammarKitConstants.BOM_CONFIGURATION_NAME,
                    "dev.thiagosouto:plugin:1.3.4",
                )

                project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).apply {
                    exclude(mapOf(
                        "group" to "org.jetbrains.plugins",
                        "module" to "ant",
                    ))
                    exclude(mapOf(
                        "group" to "org.jetbrains.plugins",
                        "module" to "idea",
                    ))
                }
                project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).apply {
                    exclude(mapOf(
                        "group" to "soutoPackage",
                        "module" to "test1",
                    ))
                }
            } else {
                configureGrammarKitClassPath(project, extension)
            }
        }
    }

    private fun configureGrammarKitClassPath(project: Project, extension: GrammarKitPluginExtension) {
        val grammarKitRelease = extension.grammarKitRelease.get()
        val jflexRelease = extension.jflexRelease.get()
        val intellijRelease = extension.intellijRelease.get()

        project.configurations.create(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME) {
            it.dependencies.addAll(listOf(
                "com.github.JetBrains:Grammar-Kit:$grammarKitRelease",
                "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                "com.jetbrains.intellij.platform:indexing-impl:$intellijRelease",
                "com.jetbrains.intellij.platform:analysis-impl:$intellijRelease",
                "com.jetbrains.intellij.platform:core-impl:$intellijRelease",
                "com.jetbrains.intellij.platform:lang-impl:$intellijRelease",
                "org.jetbrains.intellij.deps:asm-all:7.0.1",
            ).map(project.dependencies::create))

            it.exclude(mapOf("group" to "com.jetbrains.rd"))
            it.exclude(mapOf("group" to "org.jetbrains.marketplace"))
            it.exclude(mapOf("group" to "org.roaringbitmap"))
            it.exclude(mapOf("group" to "org.jetbrains.plugins"))
            it.exclude(mapOf("module" to "idea"))
            it.exclude(mapOf("module" to "ant"))
        }
    }
}
