package org.jetbrains.grammarkit.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

open class GenerateParserTask @Inject constructor(
    private val objectFactory: ObjectFactory,
    private val execOperations: ExecOperations,
) : ConventionTask() {

    @Input
    val source: Property<String> = objectFactory.property(String::class.java)

    @InputFile
    @Optional
    val sourceFile: RegularFileProperty = objectFactory.fileProperty()

    @Input
    val targetRoot: Property<String> = objectFactory.property(String::class.java)

    @OutputDirectory
    @Optional
    val targetRootOutputDir: DirectoryProperty = objectFactory.directoryProperty()

    @Input
    val pathToParser: Property<String> = objectFactory.property(String::class.java)

    @Input
    val pathToPsiRoot: Property<String> = objectFactory.property(String::class.java)

    @OutputFile
    @Optional
    val parserFile: RegularFileProperty = objectFactory.fileProperty()

    @OutputDirectory
    @Optional
    val psiDir: DirectoryProperty = objectFactory.directoryProperty()

    @Input
    @Optional
    val purgeOldFiles: Property<Boolean> = objectFactory.property(Boolean::class.java)

    @InputFiles
    @Classpath
    val classpath: ListProperty<File> = objectFactory.listProperty(File::class.java)

    @TaskAction
    fun generateParser() {
        ByteArrayOutputStream().use { os ->
            try {
                execOperations.javaexec {
                    it.mainClass.set("org.intellij.grammar.Main")
                    it.args = getArgs()
                    it.classpath = objectFactory.fileCollection().from(classpath.get())
                    it.errorOutput = os
                    it.standardOutput = os
                }
            } catch (e: Exception) {
                throw GradleException(os.toString().trim(), e)
            }

            println(os.toString())
        }
    }

    private fun getArgs() = listOf(targetRootOutputDir, sourceFile).map { it.path }
}

