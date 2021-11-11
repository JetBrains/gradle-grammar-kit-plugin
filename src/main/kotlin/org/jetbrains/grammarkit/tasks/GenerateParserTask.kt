package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

open class GenerateParserTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val projectLayout: ProjectLayout,
    private val execOperations: ExecOperations,
) : ConventionTask() {

    @Input
    val source: Property<String> = objectFactory.property(String::class.java)

    @InputFile
    val sourceFile: Provider<RegularFile> = source.map {
        projectLayout.projectDirectory.file(it)
    }

    @Input
    val targetRoot: Property<String> = objectFactory.property(String::class.java)

    @OutputDirectory
    val targetRootOutputDir: Provider<Directory> = targetRoot.map {
        projectLayout.projectDirectory.dir(it)
    }

    @Input
    val pathToParser: Property<String> = objectFactory.property(String::class.java)

    @Input
    val pathToPsiRoot: Property<String> = objectFactory.property(String::class.java)

    @OutputFile
    val parserFile: Provider<RegularFile> = pathToParser.map {
        projectLayout.projectDirectory.file("${targetRoot.get()}/$it")
    }

    @OutputDirectory
    val psiDir: Provider<Directory> = pathToPsiRoot.map {
        projectLayout.projectDirectory.dir("${targetRoot.get()}/$it")
    }

    @Input
    @Optional
    val purgeOldFiles: Property<Boolean> = objectFactory.property(Boolean::class.java)

    @InputFiles
    @Classpath
    val classpath: ConfigurableFileCollection = objectFactory.fileCollection()

    @TaskAction
    fun generateParser() {
        ByteArrayOutputStream().use { os ->
            try {
                println("classpath='${classpath.files}'")
                execOperations.javaexec {
                    it.mainClass.set("org.intellij.grammar.Main")
                    it.args = getArgs()
                    it.classpath = classpath
                    it.errorOutput = TeeOutputStream(System.out, os)
                    it.standardOutput = TeeOutputStream(System.out, os)
                }
            } catch (e: Exception) {
                throw GradleException(os.toString().trim(), e)
            }
        }
    }

    private fun getArgs() = listOf(targetRootOutputDir, sourceFile).map { it.get().asFile.canonicalPath }
}
