package org.jetbrains.grammarkit

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class GrammarKitPluginExtension @Inject constructor(
    objectFactory: ObjectFactory,
) {

    /**
     * Tag or short commit hash of Grammar-Kit to use.
     * By default, uses the latest version available.
     * See: [https://github.com/JetBrains/Grammar-Kit/releases]
     */
    @Input
    @Optional
    val grammarKitRelease: Property<String> = objectFactory.property(String::class.java)

    /**
     * Version of IntelliJ patched JFlex.
     * By default, uses the latest version available.
     * See: [https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/org/jetbrains/intellij/deps/jflex/jflex/]
     */
    @Input
    @Optional
    val jflexRelease: Property<String> = objectFactory.property(String::class.java)

    /**
     * Version of the IntelliJ to build the classpath for GenerateParser/GenerateLexer tasks.
     */
    @Input
    @Optional
    val intellijRelease: Property<String> = objectFactory.property(String::class.java)
}

