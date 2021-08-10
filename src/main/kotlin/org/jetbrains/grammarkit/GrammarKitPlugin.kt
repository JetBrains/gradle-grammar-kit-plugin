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

        extension.grammarKitRelease.set(GrammarKitConstants.GRAMMAR_KIT_DEFAULT_VERSION)
        extension.jflexRelease.set(GrammarKitConstants.JFLEX_DEFAULT_VERSION)

        project.tasks.register(GrammarKitConstants.GENERATE_LEXER_TASK_NAME, GenerateLexerTask::class.java) { task ->
            task.description = "Generates lexers for IntelliJ-based plugin"
            task.group = GrammarKitConstants.GROUP_NAME

            task.sourceFile.convention {
                project.file(task.source.get())
            }
            task.targetOutputDir.convention(
                project.layout.dir(project.provider {
                    project.file(task.targetDir.get())
                })
            )
            task.targetFile.convention {
                project.file("${task.targetDir.get()}/${task.targetClass.get()}.java")
            }

            task.doFirst {
                if (task.purgeOldFiles.orNull == true) {
                    project.delete(task.targetFile.get())
                }
            }
        }

        project.tasks.register(GrammarKitConstants.GENERATE_PARSER_TASK_NAME, GenerateParserTask::class.java) { task ->
            task.description = "Generates parsers for IntelliJ-based plugin"
            task.group = GrammarKitConstants.GROUP_NAME

            task.sourceFile.convention {
                project.file(task.source.get())
            }
            task.targetRootOutputDir.convention(
                project.layout.dir(project.provider {
                    project.file(task.targetRoot.get())
                })
            )

            task.parserFile.convention {
                project.file("${task.targetRoot.get()}/${task.pathToParser.get()}")
            }
            task.psiDir.convention(
                project.layout.dir(project.provider {
                    project.file("${task.targetRoot.get()}/${task.pathToPsiRoot.get()}")
                })
            )

            task.doFirst {
                if (task.purgeOldFiles.orNull == true) {
                    val parserFile = project.file("${task.targetRoot.get()}/${task.pathToParser.get()}")
                    val psiDir = project.file("${task.targetRoot.get()}/${task.pathToPsiRoot.get()}")

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
                it.url = URI("https://cache-redirector.jetbrains.com/intellij-repository/releases")
            }
            maven {
                it.url = URI("https://www.jitpack.io")
            }
        }

        project.afterEvaluate {
            val grammarKitRelease = extension.grammarKitRelease.get()
            val jflexRelease = extension.jflexRelease.get()
            val intellijRelease = extension.intellijRelease.orNull

            project.configurations.create(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME)

            if (intellijRelease == null) {
                project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).apply {
                    dependencies.addAll(listOf(
                        "com.github.JetBrains:Grammar-Kit:$grammarKitRelease",
                        "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                        "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                    ).map(project.dependencies::create))

                    exclude(mapOf(
                        "group" to "org.jetbrains.plugins",
                        "module" to "ant",
                    ))
                    exclude(mapOf(
                        "group" to "org.jetbrains.plugins",
                        "module" to "idea",
                    ))
                }
                project.configurations.maybeCreate(GrammarKitConstants.BOM_CONFIGURATION_NAME).apply {
                    dependencies.addAll(listOf(
                        "dev.thiagosouto:plugin:1.3.4",
                    ).map(project.dependencies::create))

                    exclude(mapOf(
                        "group" to "soutoPackage",
                        "module" to "test1",
                    ))
                }
            } else {
                configureGrammarKitClassPath(project, grammarKitRelease, jflexRelease, intellijRelease)
            }
        }
    }

    private fun configureGrammarKitClassPath(project: Project, grammarKitRelease: String, jflexRelease: String, intellijRelease: String) {
        project.configurations.getByName(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME) {
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
