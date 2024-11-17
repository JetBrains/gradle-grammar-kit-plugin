// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.grammarkit.GrammarKitConstants
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.regex.Pattern

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
     * The path to the target directory for the generated lexer.
     */
    @Deprecated(
        message = "Use targetOutputDir instead.",
        replaceWith = ReplaceWith("targetOutputDir.set(project.layout.projectDirectory.dir(targetDir))"),
        level = DeprecationLevel.ERROR
    )
    @get:Internal
    abstract val targetDir: Property<String>

    /**
     * The output directory for the generated lexer.
     * The Java file is created directly below the given directory.
     * The value of [packageName] is ignored.
     * Stale files in the given directory are *not* deleted during task execution,
     * unless [purgeOldFiles] is explicitly set to `true`.
     */
    @Deprecated(
        message = "Use targetRootOutputDir instead. " +
                "There is also a new default for the `generateLexer` task which should be sufficient for most use cases." +
                "When using the new property, stale files in `targetRootOutputDir` are deleted by default " +
                "and the Java file is created in a subdirectory matching the package of the file. " +
                "You can restore the previous behavior by adding `purgeOldFiles = false` and `packageName = \"\"`. ",
        replaceWith = ReplaceWith("targetRootOutputDir"),
        level = DeprecationLevel.WARNING,
    )
    @get:OutputDirectory
    @get:Optional
    abstract val targetOutputDir: DirectoryProperty

    /**
     * The output directory for the generated lexer.
     * The Java file for the lexer is created in a subdirectory matching the [packageName].
     * Stale files in the given directory are deleted during task execution,
     * unless [purgeOldFiles] is explicitly set to `false`.
     */
    @get:OutputDirectory
    @get:Optional
    abstract val targetRootOutputDir: DirectoryProperty

    /**
     * The name of the package where the lexer shall be created.
     * By default, the task tries to detect the package from the content of [sourceFile].
     * You may set the value to the empty string (`""`), if no subdirectory shall be created.
     * The empty string represents the unnamed package.
     */
    @get:Input
    @get:Optional
    abstract val packageName: Property<String>

    /**
     * The Java file name where the generated lexer will be written.
     */
    @Deprecated(
        message = "Setting this property has no effect, the generated lexerClass is set in the flex file.",
        level = DeprecationLevel.ERROR
    )
    @get:Internal
    abstract val targetClass: Property<String>

    /**
     * The output file computed from the [targetDir] and [targetClass] properties.
     */
    @Deprecated(
        message = """The generated lexerClass is set in the flex file.
            |Use targetOutDir if you want to get the folder or targetFile("LexerName") to get the generated lexer file""",
        replaceWith = ReplaceWith("""targetFile(targetClass)"""),
        level = DeprecationLevel.ERROR
    )
    @get:Internal
    abstract val targetFile: RegularFileProperty

    /**
     * The location of the lexer class computed from the [targetOutputDir].
     * Because the lexer class is defined in the flex file, it needs to be passed here too.
     */
    @Deprecated(
        message = "You may specify the expected output directory relative to targetRootOutputDir instead.",
        replaceWith = ReplaceWith("targetRootOutputDir.file(/* add package if necessary */ \"\${lexerClass}.java\")"),
        level = DeprecationLevel.WARNING,
    )
    fun targetFile(lexerClass: String): Provider<RegularFile> = targetOutputDir.file("${lexerClass}.java")

    /**
     * The location of the lexer class computed from the [targetOutputDir].
     * Because the lexer class is defined in the flex file, it needs to be passed here too.
     */
    @Deprecated(
        message = "You may specify the expected output directory relative to targetRootOutputDir instead.",
        replaceWith = ReplaceWith("targetRootOutputDir.file(lexerClass.map { /* add package if necessary */ \"\${it}.java\"})"),
        level = DeprecationLevel.WARNING,
    )
    fun targetFile(lexerClass: Provider<String>): Provider<RegularFile> = lexerClass.flatMap(::targetFile)

    @Deprecated(
        message = "The `source` was removed in favour of `sourceFile`.",
        replaceWith = ReplaceWith("sourceFile"),
        level = DeprecationLevel.ERROR,
    )
    @get:Internal
    abstract val source: Property<String>

    /**
     * Required.
     * The source Flex file to generate the lexer from.
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
     * By default, old files are purged unless [targetOutputDir] has been specified.
     * If you want to disable this option because the output directory is shared with another task,
     * note that you may run into issues with stale files. Also note that
     * [overlapping task outputs are discouraged by Gradle](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#avoid_overlapping_task_outputs)
     * and may cause issues when using the build cache.
     */
    @get:Input
    @get:Optional
    abstract val purgeOldFiles: Property<Boolean>

    @TaskAction
    override fun exec() {
        if (purgeOldFiles.orNull == true) {
            targetOutputDir.asFile.orNull?.deleteRecursively()
        }
        if (purgeOldFiles.orNull != false) {
            // Delete targetRootOutputDir even if the directory is overridden by `targetOutputDir`.
            // The directory may still contain stale files from a previous execution,
            // and would still be added to the source set when using `srcDir(task)`.
            targetRootOutputDir.asFile.orNull?.deleteRecursively()
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
            "-d", getOutputDirectory().path,
        )

        if (skeleton.isPresent) {
            args.add("--skel")
            args.add(skeleton.path)
        }

        args.add(sourceFile.path)

        return args
    }

    /**
     * Resolves the correct output directory, considering the package of the lexer file.
     */
    private fun getOutputDirectory(): Provider<Directory> {
        // `targetOutputDir` takes precedence for backwards compatibility
        return if (targetOutputDir.isPresent) {
            targetOutputDir
        } else if (targetRootOutputDir.isPresent) {
            val packageProvider = packageName.orElse(sourceFile.map(::readPackageDeclaration))
            targetRootOutputDir.zip(packageProvider) { rootDir, pkg ->
                if (pkg.isEmpty()) {
                    rootDir
                } else {
                    rootDir.dir(pkg.replace('.', File.separatorChar))
                }
            }
        } else {
            throw GradleException("""
                Neither of the properties 'targetOutputDir' and 'targetRootOutputDir' have a configured value.
            """.trimIndent())
        }
    }

    /**
     * Try to find and read the package declaration in the given source file.
     * @return the package name or the empty string
     */
    private fun readPackageDeclaration(source: RegularFile): String {
        // The Maven plugin of JFlex 1.9.1 uses a similar approach to detect the package name:
        // https://github.com/jflex-de/jflex/blob/7b36a83dfb4502c69a7da09cfd15db9c8dd5b701/jflex-maven-plugin/src/main/java/jflex/maven/plugin/jflex/LexSimpleAnalyzerUtils.java#L163-L176
        // https://github.com/jflex-de/jflex/blob/7b36a83dfb4502c69a7da09cfd15db9c8dd5b701/jflex-maven-plugin/src/main/java/jflex/maven/plugin/jflex/SpecInfo.java#L63-L70
        // As well as the Ant Task:
        // https://github.com/jflex-de/jflex/blob/7b36a83dfb4502c69a7da09cfd15db9c8dd5b701/jflex/src/main/java/jflex/anttask/JFlexTask.java#L111-L116
        // https://github.com/jflex-de/jflex/blob/7b36a83dfb4502c69a7da09cfd15db9c8dd5b701/jflex/src/main/java/jflex/anttask/JFlexTask.java#L150-L155
        val packagePattern = Pattern.compile("\\s*package\\s+(\\S+)\\s*;.*")
        source.asFile.useLines { lines ->
            lines.forEach { line ->
                val matcher = packagePattern.matcher(line)
                if (matcher.matches()) {
                    return matcher.group(1)
                }
            }
        }
        logger.warn("Could not detect `packageName` for $path, the lexer will be generated in the directory of the unnamed package")
        return ""
    }
}
