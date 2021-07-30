package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

class GrammarKitPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def grammarKitExtension = project.extensions.create(
                GrammarKitConstants.GROUP_NAME,
                GrammarKitPluginExtension.class,
        )

        project.tasks.register(GrammarKitConstants.GENERATE_LEXER_TASK_NAME, GenerateLexerTask.class) {
            it.description = "Generates lexers for IntelliJ-based plugin"
            it.group = GrammarKitConstants.GROUP_NAME

            def targetFile = project.file("${it.targetDir}/${it.targetClass}.java")
            it.targetFile = targetFile

            doFirst {
                if (it.purgeOldFiles) {
                    project.delete(targetFile)
                }
            }
        }

        project.tasks.register(GrammarKitConstants.GENERATE_PARSER_TASK_NAME, GenerateParserTask.class) {
            it.description = "Generates parsers for IntelliJ-based plugin"
            it.group = GrammarKitConstants.GROUP_NAME

//            it.parserFile = project.file("$it.targetRoot/$it.pathToParser")
//            it.psiDir = project.file("$it.targetRoot/$it.pathToPsiRoot")

            doFirst {
                if (it.purgeOldFiles) {
                    def parserFile = project.file("${it.targetRoot}/${it.pathToParser}")
                    def psiDir = project.file("${it.targetRoot}/${it.pathToPsiRoot}")

                    project.delete(parserFile)
                    project.delete(psiDir)
                }
            }
        }

        project.repositories {
            maven { url "https://cache-redirector.jetbrains.com/intellij-dependencies" }
            maven { url "https://www.jetbrains.com/intellij-repository/releases" }
            maven { url 'https://www.jitpack.io' }
        }
        project.afterEvaluate {
            if (grammarKitExtension.intellijRelease == null) {
                project.dependencies.add(
                        JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                        "com.github.JetBrains:Grammar-Kit:${grammarKitExtension.grammarKitRelease}",
                        {
                            exclude group: 'org.jetbrains.plugins'
                            exclude module: 'idea'
                        })
                project.dependencies.add(
                        JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                        "org.jetbrains.intellij.deps.jflex:jflex:${grammarKitExtension.jflexRelease}",
                        {
                            exclude group: 'org.jetbrains.plugins'
                            exclude module: 'idea'
                            exclude module: 'ant'
                        }
                )
            } else {
                configureGrammarKitClassPath()
            }
        }
    }

    void configureGrammarKitClassPath(Project project) {
        project.configurations {
            grammarKitClassPath
        }
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "com.github.JetBrains:Grammar-Kit:${grammarKitExtension.grammarKitRelease}"
        )
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "org.jetbrains.intellij.deps.jflex:jflex:${grammarKitExtension.jflexRelease}"
        )
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "com.jetbrains.intellij.platform:indexing-impl:${grammarKitExtension.intellijRelease}"
        )
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "com.jetbrains.intellij.platform:analysis-impl:${grammarKitExtension.intellijRelease}"
        )
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "com.jetbrains.intellij.platform:core-impl:${grammarKitExtension.intellijRelease}"
        )
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "com.jetbrains.intellij.platform:lang-impl:${grammarKitExtension.intellijRelease}"
        )
        project.dependencies.add(
                project.configurations.grammarKitClassPath.name,
                "org.jetbrains.intellij.deps:asm-all:7.0.1"
        )
        project.configurations.grammarKitClassPath {
            exclude group: 'com.jetbrains.rd'
            exclude group: 'org.jetbrains.marketplace'
            exclude group: 'org.roaringbitmap'
            exclude group: 'org.jetbrains.plugins'
            exclude module: 'idea'
            exclude module: 'ant'
        }
    }
}
