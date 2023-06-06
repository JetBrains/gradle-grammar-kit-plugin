// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.*
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

    @Deprecated(
        message = "The `source` was removed in favour of `sourceFile`.",
        replaceWith = ReplaceWith("sourceFile"),
        level = DeprecationLevel.ERROR,
    )
    @get:Internal
    abstract val source: Property<String>

    /**
     * Required.
     * The source BNF file to generate the parser from.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFile: RegularFileProperty

    /**
     * The path to the target directory for the generated parser.
     */
    @Deprecated(
        message = "The `targetRoot` was removed in favour of `targetRootOutputDir`.",
        replaceWith = ReplaceWith("targetRootOutputDir"),
        level = DeprecationLevel.ERROR,
    )
    @get:Internal
    abstract val targetRoot: Property<String>

    /**
     * Required.
     * The output root directory.
     */
    @get:OutputDirectory
    abstract val targetRootOutputDir: DirectoryProperty

    /**
     * Required.
     * The location of the generated parser class, relative to the [targetRootOutputDir].
     */
    @get:Input
    abstract val pathToParser: Property<String>

    /**
     * Required.
     * The location of the generated PSI files, relative to the [targetRoot].
     */
    @get:Input
    abstract val pathToPsiRoot: Property<String>

    /**
     * The output parser file computed from the [pathToParser] property.
     */
    fun parserFile(): Provider<RegularFile> = targetRootOutputDir.file(pathToParser)

    /**
     * The output PSI directory computed from the [pathToPsiRoot] property.
     */
    fun psiDir(): Provider<Directory> = targetRootOutputDir.dir(pathToPsiRoot)

    /**
     * Purge old files from the target directory before generating the lexer.
     */
    @get:Input
    @get:Optional
    abstract val purgeOldFiles: Property<Boolean>

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
