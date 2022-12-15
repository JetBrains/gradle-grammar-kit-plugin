// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.grammarkit.path
import org.jetbrains.grammarkit.GrammarKitConstants
import java.io.ByteArrayOutputStream

/**
 * The `generateLexer` task generates a lexer for the given grammar.
 * The task is configured using common [org.jetbrains.grammarkit.GrammarKitPluginExtension] extension.
 */
@CacheableTask
abstract class GenerateLexerTask : JavaExec() {

    init {
        description = "Generates lexers for IntelliJ-based plugin"
        group = GrammarKitConstants.GROUP_NAME

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
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFile: RegularFileProperty

    /**
     * An optional path to the skeleton file to use for the generated lexer.
     * The path will be provided as `--skel` option.
     * By default, it uses the [`idea-flex.skeleton`](https://raw.github.com/JetBrains/intellij-community/master/tools/lexer/idea-flex.skeleton) skeleton file.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    abstract val skeleton: RegularFileProperty

    /**
     * Purge old files from the target directory before generating the lexer.
     */
    @get:Input
    @get:Optional
    abstract val purgeOldFiles: Property<Boolean>

    init {
        targetOutputDir.convention(targetDir.map {
            project.layout.projectDirectory.dir(it)
        })
        targetFile.convention(targetClass.map {
            project.layout.projectDirectory.file("${targetDir.get()}/$it.java")
        })
        sourceFile.convention(source.map {
            project.layout.projectDirectory.file(it)
        })
    }

    @TaskAction
    override fun exec() {
        if (purgeOldFiles.orNull == true) {
            targetFile.get().asFile.deleteRecursively()
        }
        ByteArrayOutputStream().use { os ->
            try {
                args = getArguments()
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
