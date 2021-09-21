package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginInstantiationException
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import java.io.File
import java.net.URI

@Suppress("unused")
open class GrammarKitPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkGradleVersion(project)

        val extension = project.extensions.create(
            GrammarKitConstants.GROUP_NAME,
            GrammarKitPluginExtension::class.java,
        )

        extension.grammarKitRelease.set(GrammarKitConstants.GRAMMAR_KIT_DEFAULT_VERSION)
        extension.jflexRelease.set(GrammarKitConstants.JFLEX_DEFAULT_VERSION)

        val grammarKitClassPathConfiguration =
            project.configurations.create(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME)
        val compileClasspathConfiguration = project.configurations.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
        val compileOnlyConfiguration = project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
        val bomConfiguration = project.configurations.maybeCreate(GrammarKitConstants.BOM_CONFIGURATION_NAME)

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
            task.classpath.convention(project.provider {
                getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                    file.name.startsWith("jflex")
                }
            })

            task.doFirst {
                if (task.purgeOldFiles.orNull == true) {
                    task.targetFile.asFile.get().deleteRecursively()
                }
            }
        }

        project.tasks.register(GrammarKitConstants.GENERATE_PARSER_TASK_NAME, GenerateParserTask::class.java) { task ->
            task.description = "Generates parsers for IntelliJ-based plugin"
            task.group = GrammarKitConstants.GROUP_NAME

            val requiredLibs = listOf(
                "jdom", "trove4j", "junit", "guava", "asm-all", "automaton", "java-api", "platform-api", "platform-impl",
                "util", "annotations", "picocontainer", "extensions", "idea", "openapi", "Grammar-Kit",
                "platform-util-ui", "platform-concurrency", "intellij-deps-fastutil",
                // CLion unlike IDEA contains `MockProjectEx` in `testFramework.jar` instead of `idea.jar`
                // so this jar should be in `requiredLibs` list to avoid `NoClassDefFoundError` exception
                // while parser generation with CLion distribution
                "testFramework", "3rd-party"
            )

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
            task.classpath.convention(project.provider {
                getClasspath(grammarKitClassPathConfiguration, compileClasspathConfiguration) { file ->
                    requiredLibs.any {
                        file.name.equals("$it.jar", true) || file.name.startsWith("$it-")
                    }
                }
            })

            task.doFirst {
                if (task.purgeOldFiles.orNull == true) {
                    task.targetRootOutputDir.asFile.get().apply {
                        resolve(task.pathToParser.get()).deleteRecursively()
                        resolve(task.pathToPsiRoot.get()).deleteRecursively()
                    }
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

            if (intellijRelease == null) {
                compileOnlyConfiguration.apply {
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
                bomConfiguration.apply {
                    dependencies.addAll(listOf(
                        "dev.thiagosouto:plugin:1.3.4",
                    ).map(project.dependencies::create))

                    exclude(mapOf(
                        "group" to "soutoPackage",
                        "module" to "test1",
                    ))
                }
            } else {
                configureGrammarKitClassPath(project, grammarKitClassPathConfiguration, grammarKitRelease, jflexRelease, intellijRelease)
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
            dependencies.addAll(listOf(
                "com.github.JetBrains:Grammar-Kit:$grammarKitRelease",
                "org.jetbrains.intellij.deps.jflex:jflex:$jflexRelease",
                "com.jetbrains.intellij.platform:indexing-impl:$intellijRelease",
                "com.jetbrains.intellij.platform:analysis-impl:$intellijRelease",
                "com.jetbrains.intellij.platform:core-impl:$intellijRelease",
                "com.jetbrains.intellij.platform:lang-impl:$intellijRelease",
                "org.jetbrains.intellij.deps:asm-all:7.0.1",
            ).map(project.dependencies::create))

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
        if (Version.parse(project.gradle.gradleVersion) < Version.parse("6.6")) {
            throw PluginInstantiationException("gradle-grammarkit-plugin requires Gradle 6.6 and higher")
        }
    }
}
