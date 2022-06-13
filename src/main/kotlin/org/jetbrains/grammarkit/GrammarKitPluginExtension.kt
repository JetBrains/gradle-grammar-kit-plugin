// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class GrammarKitPluginExtension @Inject constructor(
    objectFactory: ObjectFactory,
) {

    /**
     * The release version of the [Grammar-Kit](https://github.com/JetBrains/Grammar-Kit) to use.
     *
     * Default value: `2021.1.2`
     */
    @Input
    @Optional
    val grammarKitRelease = objectFactory.property<String>()

    /**
     * The version of the IntelliJ-patched JFlex, a [fork of JFlex](https://github.com/JetBrains/intellij-deps-jflex)
     * lexer generator for IntelliJ Platform API.
     *
     * Default value: `1.7.0-1`
     */
    @Input
    @Optional
    val jflexRelease = objectFactory.property<String>()

    /**
     * Version of the IntelliJ to build the classpath for [org.jetbrains.grammarkit.tasks.GenerateParserTask]
     * and [org.jetbrains.grammarkit.tasks.GenerateLexerTask] tasks.
     * If provided, [grammarKitRelease] and [jflexRelease] properties are ignored as both dependencies will be provided
     * from the given IntelliJ IDEA release.
     *
     * Default value: `null`
     */
    @Input
    @Optional
    val intellijRelease = objectFactory.property<String>()
}
