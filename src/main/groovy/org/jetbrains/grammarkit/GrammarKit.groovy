package org.jetbrains.grammarkit

import org.gradle.api.Plugin
import org.gradle.api.Project

class GrammarKit implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.repositories.maven { url 'https://jitpack.io' }
        target.configurations.create('jflex')
        def grammarKitExtension = target.extensions.create("grammarKit", GrammarKitPluginExtension.class)

        target.afterEvaluate {
            target.dependencies.add(
                    JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                    "com.github.JetBrains:Grammar-Kit:${grammarKitExtension.grammarKitRelease}",
                    {
                        exclude group: 'org.jetbrains.plugins'
                        exclude module: 'idea'
                    })
            target.dependencies.add(
                    'jflex',
                    "com.github.hurricup.jflex:jflex:${grammarKitExtension.jflexRelease}",
                    {
                        exclude group: 'org.jetbrains.plugins'
                        exclude module: 'idea'
                    }
            )
        }
    }
}
