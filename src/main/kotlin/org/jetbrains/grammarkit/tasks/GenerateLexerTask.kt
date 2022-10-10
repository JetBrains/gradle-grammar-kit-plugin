// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream

/**
 * The `generateLexer` task generates a lexer for the given grammar.
 * The task is configured using common [org.jetbrains.grammarkit.GrammarKitPluginExtension] extension.
 */
abstract class GenerateLexerTask : JavaExec() {

    init {
        mainClass.set("jflex.Main")
    }

    /**
     * Required.
     * The path to the target directory for the generated lexer.
     */
    @get:Input
    abstract val targetDir: Property<String>

    /**
     * The output directory computed from the [targetDir] property.
     */
    @get:OutputDirectory
    abstract val targetOutputDir: DirectoryProperty

    /**
     * Required.
     * The Java file name where the generated lexer will be written.
     */
    @get:Input
    abstract val targetClass: Property<String>

    /**
     * The output file computed from the [targetDir] and [targetClass] properties.
     */
    @get:OutputFile
    abstract val targetFile: RegularFileProperty

    /**
     * Required.
     * The source Flex file to generate the lexer from.
     */
    @get:Input
    abstract val source: Property<String>

    /**
     * Source file computed from [source] path.
     */
    @get:InputFile
    abstract val sourceFile: RegularFileProperty

    /**
     * An optional path to the skeleton file to use for the generated lexer.
     * The path will be provided as `--skel` option.
     * By default, it uses the [`idea-flex.skeleton`](https://raw.github.com/JetBrains/intellij-community/master/tools/lexer/idea-flex.skeleton) skeleton file.
     */
    @get:InputFile
    @get:Optional
    abstract val skeleton: RegularFileProperty

    /**
     * Purge old files from the target directory before generating the lexer.
     */
    @get:Input
    @get:Optional
    abstract val purgeOldFiles: Property<Boolean>

    /**
     * The classpath with JFlex to use for the generation.
     */
    @get:InputFiles
    @get:Classpath
    abstract val jFlexClasspath: ConfigurableFileCollection

    @TaskAction
    override fun exec() {
        ByteArrayOutputStream().use { os ->
            try {
                args = getArguments()
                classpath = this@GenerateLexerTask.jFlexClasspath
                errorOutput = TeeOutputStream(System.out, os)
                standardOutput = TeeOutputStream(System.out, os)
                super.exec()
            } catch (e: Exception) {
                throw GradleException(os.toString().trim(), e)
            }
        }
    }

    private fun getArguments(): List<String> {
        val args = mutableListOf(
            "-d", targetOutputDir.path,
        )

        if (skeleton.isPresent) {
            args.add("--skel")
            args.add(skeleton.path)
        }

        args.add(sourceFile.path)

        return args
    }
}
