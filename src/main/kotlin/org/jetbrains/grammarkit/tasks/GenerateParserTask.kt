package org.jetbrains.grammarkit.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject

open class GenerateParserTask @Inject constructor(objectFactory: ObjectFactory) : ConventionTask() {

    @InputFile
    val source: RegularFileProperty = objectFactory.fileProperty()

    @OutputDirectory
    val targetRoot: DirectoryProperty = objectFactory.directoryProperty()

    @Input
    val pathToParser: Property<String> = objectFactory.property(Boolean::class.java)

    @Input
    val pathToPsiRoot: Property<String> = objectFactory.property(Boolean::class.java)

    @OutputFile
    @Optional
    val parserFile: RegularFileProperty = objectFactory.fileProperty()
//    def parserFile = project.file("$targetRoot/$pathToParser")

    @OutputDirectory
    @Optional
    val psiDir: DirectoryProperty = objectFactory.directoryProperty()
//    def psiDir = project.file("$targetRoot/$pathToPsiRoot")

    @Input
    @Optional
    val purgeOldFiles: Property<String> = objectFactory.property(Boolean::class.java)

    @TaskAction
    fun generateParser() {
        val requiredLibs = listOf(
            "jdom", "trove4j", "junit", "guava", "asm-all", "automaton", "platform-api", "platform-impl",
            "util", "annotations", "picocontainer", "extensions", "idea", "openapi", "Grammar-Kit",
            "platform-util-ui", "platform-concurrency", "intellij-deps-fastutil",
            // CLion unlike IDEA contains `MockProjectEx` in `testFramework.jar` instead of `idea.jar`
            // so this jar should be in `requiredLibs` list to avoid `NoClassDefFoundError` exception
            // while parser generation with CLion distribution
            "testFramework", "3rd-party"
        )

        ByteArrayOutputStream().use { os ->
            try {
                project.javaexec {
                    it.mainClass.set("org.intellij.grammar.Main")
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
        return listOf(project.file(targetRoot), source)
    }

    private fun getClasspath(): FileCollection {
        if (project.configurations.hasProperty("grammarKitClassPath")) {
            return project.configurations.grammarKitClassPath
        } else {
            return project.configurations.compileClasspath.files.findAll({
                for (lib in requiredLibs) {
                    if (it.name.equalsIgnoreCase("${lib}.jar") || it.name.startsWith("${lib}-")) {
                        return true;
                    }
                }
                return false;
            })
        }
    }
}

