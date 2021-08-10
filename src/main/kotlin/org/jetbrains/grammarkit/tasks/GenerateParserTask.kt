package org.jetbrains.grammarkit.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.grammarkit.GrammarKitConstants
import org.jetbrains.grammarkit.path
import java.io.ByteArrayOutputStream
import javax.inject.Inject

open class GenerateParserTask @Inject constructor(
    private val objectFactory: ObjectFactory,
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

    private val requiredLibs = listOf(
        "jdom", "trove4j", "junit", "guava", "asm-all", "automaton", "platform-api", "platform-impl",
        "util", "annotations", "picocontainer", "extensions", "idea", "openapi", "Grammar-Kit",
        "platform-util-ui", "platform-concurrency", "intellij-deps-fastutil",
        // CLion unlike IDEA contains `MockProjectEx` in `testFramework.jar` instead of `idea.jar`
        // so this jar should be in `requiredLibs` list to avoid `NoClassDefFoundError` exception
        // while parser generation with CLion distribution
        "testFramework", "3rd-party"
    )

    @TaskAction
    fun generateParser() {
        ByteArrayOutputStream().use { os ->
            try {
                project.javaexec {
                    it.mainClass.set("org.intellij.grammar.Main")
                    it.args = getArgs()
                    it.classpath = getClasspath()
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

    private fun getClasspath(): FileCollection {
        val grammarKitClassPathConfiguration = project.configurations.getByName(GrammarKitConstants.GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME)
        val compileClasspathConfiguration = project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

        return when {
            !grammarKitClassPathConfiguration.isEmpty -> grammarKitClassPathConfiguration
            else -> compileClasspathConfiguration.files.filter { file ->
                requiredLibs.any {
                    file.name.equals("$it.jar", true) || file.name.startsWith("$it-")
                }
            }.run {
                objectFactory.fileCollection().from(this)
            }
        }
    }
}

