package org.jetbrains.grammarkit.tasks

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
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
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream
import javax.inject.Inject

open class GenerateLexerTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val projectLayout: ProjectLayout,
    private val execOperations: ExecOperations,
) : ConventionTask() {

    @Input
    val targetDir: Property<String> = objectFactory.property(String::class.java)

    @OutputDirectory
    val targetOutputDir: Provider<Directory> = targetDir.map {
        projectLayout.projectDirectory.dir(it)
    }

    @Input
    val targetClass: Property<String> = objectFactory.property(String::class.java)

    @OutputFile
    val targetFile: Provider<RegularFile> = targetClass.map {
        projectLayout.projectDirectory.file("${targetDir.get()}/$it.java")
    }

    @Input
    val source: Property<String> = objectFactory.property(String::class.java)

    @InputFile
    val sourceFile: Provider<RegularFile> = source.map {
        projectLayout.projectDirectory.file(it)
    }

    @InputFile
    @Optional
    val skeleton: RegularFileProperty = objectFactory.fileProperty()

    @Input
    @Optional
    val purgeOldFiles: Property<Boolean> = objectFactory.property(Boolean::class.java)

    @InputFiles
    @Classpath
    val classpath: ConfigurableFileCollection = objectFactory.fileCollection()

    @TaskAction
    fun generateLexer() {
        ByteArrayOutputStream().use { os ->
            try {
                execOperations.javaexec {
                    it.mainClass.set("jflex.Main")
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

    private fun getArgs(): List<String> {
        val args = mutableListOf(
            "-d", targetOutputDir.get().asFile.canonicalPath,
        )

        if (skeleton.isPresent) {
            args.add("--skel")
            args.add(skeleton.path)
        }

        args.add(sourceFile.get().asFile.canonicalPath)

        return args
    }
}
