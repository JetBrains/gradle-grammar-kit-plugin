// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.grammarkit.*
import java.io.ByteArrayOutputStream

/**
 * The `generateParser` task generates a parser for the given grammar.
 * The task is configured using common [org.jetbrains.grammarkit.GrammarKitPluginExtension] extension.
 */
@CacheableTask
abstract class GenerateParserTask : JavaExec() {

    init {
        description = "Generates parsers for IntelliJ-based plugin"
        group = GrammarKitConstants.GROUP_NAME

        mainClass.set("org.intellij.grammar.Main")
    }

    /**
     * The source BNF file to generate the parser from.
     */
    @get:Input
    abstract val source: Property<String>

    /**
     * The source file computed from the [source] property.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFile: RegularFileProperty

    /**
     * The path to the target directory for the generated parser.
     */
    @get:Input
    abstract val targetRoot: Property<String>

    /**
     * The output root directory computed from the [targetRoot] property.
     */
    @get:OutputDirectory
    abstract val targetRootOutputDir: DirectoryProperty

    /**
     * The location of the generated parser class, relative to the [targetRoot].
     */
    @get:Input
    abstract val pathToParser: Property<String>

    /**
     * The location of the generated PSI files, relative to the [targetRoot].
     */
    @get:Input
    abstract val pathToPsiRoot: Property<String>

    /**
     * The output parser file computed from the [pathToParser] property.
     */
    @get:OutputFile
    abstract val parserFile: RegularFileProperty

    /**
     * The output PSI directory computed from the [pathToPsiRoot] property.
     */
    @get:OutputDirectory
    abstract val psiDir: DirectoryProperty

    /**
     * Purge old files from the target directory before generating the lexer.
     */
    @get:Input
    @get:Optional
    abstract val purgeOldFiles: Property<Boolean>

    init {
        sourceFile.convention(source.map {
            project.layout.projectDirectory.file(it)
        })
        targetRootOutputDir.convention(targetRoot.map {
            project.layout.projectDirectory.dir(it)
        })
        parserFile.convention(pathToParser.map {
            project.layout.projectDirectory.file("${targetRoot.get()}/$it")
        })
        psiDir.convention(pathToPsiRoot.map {
            project.layout.projectDirectory.dir("${targetRoot.get()}/$it")
        })
    }

    @TaskAction
    override fun exec() {
        if (purgeOldFiles.orNull == true) {
            targetRootOutputDir.get().asFile.apply {
                resolve(pathToParser.get()).deleteRecursively()
                resolve(pathToPsiRoot.get()).deleteRecursively()
            }
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

    private fun getArguments() = listOf(targetRootOutputDir, sourceFile).map { it.path }
}
