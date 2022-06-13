// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * The `generateLexer` task generates a lexer for the given grammar.
 * The task is configured using common [org.jetbrains.grammarkit.GrammarKitPluginExtension] extension.
 */
open class GenerateLexerTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val projectLayout: ProjectLayout,
    private val execOperations: ExecOperations,
) : ConventionTask() {

    /**
     * Required.
     * The path to the target directory for the generated lexer.
     */
    @Input
    val targetDir = objectFactory.property<String>()

    /**
     * The output directory computed from the [targetDir] property.
     */
    @OutputDirectory
    val targetOutputDir = targetDir.map {
        projectLayout.projectDirectory.dir(it)
    }

    /**
     * Required.
     * The Java file name where the generated lexer will be written.
     */
    @Input
    val targetClass = objectFactory.property<String>()

    /**
     * The output file computed from the [targetDir] and [targetClass] properties.
     */
    @OutputFile
    val targetFile = targetClass.map {
        projectLayout.projectDirectory.file("${targetDir.get()}/$it.java")
    }

    /**
     * Required.
     * The source Flex file to generate the lexer from.
     */
    @Input
    val source = objectFactory.property<String>()

    /**
     * Source file computed from [source] path.
     */
    @InputFile
    val sourceFile = source.map {
        projectLayout.projectDirectory.file(it)
    }

    /**
     * An optional path to the skeleton file to use for the generated lexer.
     * The path will be provided as `--skel` option.
     * By default, it uses the [`idea-flex.skeleton`](https://raw.github.com/JetBrains/intellij-community/master/tools/lexer/idea-flex.skeleton) skeleton file.
     */
    @InputFile
    @Optional
    val skeleton = objectFactory.fileProperty()

    /**
     * Purge old files from the target directory before generating the lexer.
     */
    @Input
    @Optional
    val purgeOldFiles = objectFactory.property<Boolean>()

    /**
     * The classpath with JFlex to use for the generation.
     */
    @InputFiles
    @Classpath
    val classpath = objectFactory.fileCollection()

    @TaskAction
    fun generateLexer() {
        ByteArrayOutputStream().use { os ->
            try {
                execOperations.javaexec {
                    mainClass.set("jflex.Main")
                    args = getArguments()
                    classpath = this@GenerateLexerTask.classpath
                    errorOutput = TeeOutputStream(System.out, os)
                    standardOutput = TeeOutputStream(System.out, os)
                }
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
