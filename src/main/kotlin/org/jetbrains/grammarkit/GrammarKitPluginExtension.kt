// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

abstract class GrammarKitPluginExtension {

    /**
     * The release version of the [Grammar-Kit](https://github.com/JetBrains/Grammar-Kit) to use.
     *
     * Default value: `2022.3.2`
     */
    @get:Input
    @get:Optional
    abstract val grammarKitRelease: Property<String>

    /**
     * The version of the IntelliJ-patched JFlex, a [fork of JFlex](https://github.com/JetBrains/intellij-deps-jflex)
     * lexer generator for IntelliJ Platform API.
     *
     * Default value: 1.9.2
     */
    @get:Input
    @get:Optional
    abstract val jflexRelease: Property<String>

    /**
     * Version of the IntelliJ to build the classpath for [GenerateParserTask] and [GenerateLexerTask] tasks.
     * If provided, [grammarKitRelease] and [jflexRelease] properties are ignored as both dependencies will be provided
     * from the given IntelliJ IDEA release.
     *
     * Default value: `null`
     */
    @get:Input
    @get:Optional
    abstract val intellijRelease: Property<String>

    /**
     * The source file for `:generateLexer`.
     * When this property is used, the output directory of the task will automatically be added to the main source set.
     * If this property is set, the plugin ...
     *
     * - uses the value as the default for [`tasks.generateLexer.sourceFile`][GenerateLexerTask.sourceFile].
     * - calls `sourceSets.main.java.srcDir(tasks.generateLexer)`
     *
     * Default value: `null`
     */
    @get:InputFile
    @get:Optional
    abstract val lexerSource: RegularFileProperty

    /**
     * The source file for `:generateParser`.
     * When this property is used, the output directory of the task will automatically be added to the main source set.
     * If this property is set, the plugin ...
     *
     * - uses the value as the default for [`tasks.generateParser.sourceFile`][GenerateParserTask.sourceFile].
     * - calls `sourceSets.main.java.srcDir(tasks.generateParser)`
     *
     * Default value: `null`
     */
    @get:InputFile
    @get:Optional
    abstract val parserSource: RegularFileProperty

    init {
        grammarKitRelease.convention(GrammarKitConstants.GRAMMAR_KIT_DEFAULT_VERSION)
        jflexRelease.convention(GrammarKitConstants.JFLEX_DEFAULT_VERSION)
        intellijRelease.convention("")
    }
}
