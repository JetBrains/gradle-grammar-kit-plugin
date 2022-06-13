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
 * The `generateParser` task generates a parser for the given grammar.
 * The task is configured using common [org.jetbrains.grammarkit.GrammarKitPluginExtension] extension.
 */
open class GenerateParserTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val projectLayout: ProjectLayout,
    private val execOperations: ExecOperations,
) : ConventionTask() {

    /**
     * The source BNF file to generate the parser from.
     */
    @Input
    val source = objectFactory.property<String>()

    /**
     * The source file computed from the [source] property.
     */
    @InputFile
    val sourceFile = source.map {
        projectLayout.projectDirectory.file(it)
    }

    /**
     * The path to the target directory for the generated parser.
     */
    @Input
    val targetRoot = objectFactory.property<String>()

    /**
     * The output root directory computed from the [targetRoot] property.
     */
    @OutputDirectory
    val targetRootOutputDir = targetRoot.map {
        projectLayout.projectDirectory.dir(it)
    }

    /**
     * The location of the generated parser class, relative to the [targetRoot].
     */
    @Input
    val pathToParser = objectFactory.property<String>()

    /**
     * The location of the generated PSI files, relative to the [targetRoot].
     */
    @Input
    val pathToPsiRoot = objectFactory.property<String>()

    /**
     * The output parser file computed from the [pathToParser] property.
     */
    @OutputFile
    val parserFile = pathToParser.map {
        projectLayout.projectDirectory.file("${targetRoot.get()}/$it")
    }

    /**
     * The output PSI directory computed from the [pathToPsiRoot] property.
     */
    @OutputDirectory
    val psiDir = pathToPsiRoot.map {
        projectLayout.projectDirectory.dir("${targetRoot.get()}/$it")
    }

    /**
     * Purge old files from the target directory before generating the lexer.
     */
    @Input
    @Optional
    val purgeOldFiles = objectFactory.property<Boolean>()

    /**
     * The classpath with Grammar-Kit to use for the generation.
     */
    @InputFiles
    @Classpath
    val classpath = objectFactory.fileCollection()

    @TaskAction
    fun generateParser() {
        ByteArrayOutputStream().use { os ->
            try {
                execOperations.javaexec {
                    mainClass.set("org.intellij.grammar.Main")
                    args = getArguments()
                    classpath = this@GenerateParserTask.classpath
                    errorOutput = TeeOutputStream(System.out, os)
                    standardOutput = TeeOutputStream(System.out, os)
                }
            } catch (e: Exception) {
                throw GradleException(os.toString().trim(), e)
            }
        }
    }

    private fun getArguments() = listOf(targetRootOutputDir, sourceFile).map { it.path }
}
