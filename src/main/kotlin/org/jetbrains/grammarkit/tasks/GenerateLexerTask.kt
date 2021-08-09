package org.jetbrains.grammarkit.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream
import javax.inject.Inject

open class GenerateLexerTask @Inject constructor(
    private val objectFactory: ObjectFactory,
) : ConventionTask() {

    @OutputDirectory
    val targetDir: DirectoryProperty = objectFactory.directoryProperty()

    @OutputFile
    val targetFile: RegularFileProperty = objectFactory.fileProperty()

    @Input
    val targetClass: Property<String> = objectFactory.property(String::class.java)

    @InputFile
    val source: RegularFileProperty = objectFactory.fileProperty()

    @InputFile
    @Optional
    val skeleton: RegularFileProperty = objectFactory.fileProperty()

    @Input
    @Optional
    val purgeOldFiles: Property<Boolean> = objectFactory.property(Boolean::class.java)

    @TaskAction
    fun generateLexer() {
        ByteArrayOutputStream().use { os ->
            try {
                project.javaexec {
                    it.mainClass.set("jflex.Main")
                    it.args = getArgs()
                    it.classpath = getClasspath()
                    it.standardOutput = os
                }
            } catch (e: Exception) {
                throw GradleException(os.toString().trim(), e)
            }

            println(os.toString())
        }
    }

    private fun getArgs(): List<String> {
        val args = mutableListOf(
            "-d", targetDir.path,
        )

        if (skeleton.isPresent) {
            args.add("--skel")
            args.add(skeleton.path)
        }

        args.add(source.path)

        return args
    }

    private fun getClasspath(): FileCollection {
        val grammarKitClassPathConfiguration = project.configurations.getByName("grammarKitClassPath")
        val compileClasspathConfiguration = project.configurations.getByName("grammarKitClassPath")

        return when {
            !grammarKitClassPathConfiguration.isEmpty -> grammarKitClassPathConfiguration
            else -> compileClasspathConfiguration.files.filter {
                it.name.startsWith("jflex")
            }.run {
                objectFactory.fileCollection().from(this)
            }
        }
    }
}
