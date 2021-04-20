package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class GrammarKit implements Plugin<Project> {
    @Override
    void apply(Project target) {
        def grammarKitExtension = target.extensions.create("grammarKit", GrammarKitPluginExtension.class)
        target.configurations {
          grammarKitClassPath
        }
        target.afterEvaluate {
            target.repositories {
                maven { url "https://cache-redirector.jetbrains.com/intellij-dependencies" }
                maven { url "https://www.jetbrains.com/intellij-repository/releases" }
                maven { url 'https://www.jitpack.io' }
            }
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "com.github.JetBrains:Grammar-Kit:${grammarKitExtension.grammarKitRelease}"
            )
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "org.jetbrains.intellij.deps.jflex:jflex:${grammarKitExtension.jflexRelease}"
            )
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "com.jetbrains.intellij.platform:indexing-impl:${grammarKitExtension.intellijRelease}"
            )
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "com.jetbrains.intellij.platform:analysis-impl:${grammarKitExtension.intellijRelease}"
            )
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "com.jetbrains.intellij.platform:core-impl:${grammarKitExtension.intellijRelease}"
            )
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "com.jetbrains.intellij.platform:lang-impl:${grammarKitExtension.intellijRelease}"
            )
            target.dependencies.add(
                    target.configurations.grammarKitClassPath.name,
                    "org.jetbrains.intellij.deps:asm-all:7.0.1"
            )
            target.configurations.grammarKitClassPath {
              exclude group: 'com.jetbrains.rd'
              exclude group: 'org.jetbrains.marketplace'
              exclude group: 'org.roaringbitmap'
              exclude group: 'org.jetbrains.plugins'
              exclude module: 'idea'
              exclude module: 'ant'
            }
        }
    }
}
