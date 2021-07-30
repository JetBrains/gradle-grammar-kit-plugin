package org.jetbrains.grammarkit.tasks

import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.*

class GenerateParserTask extends ConventionTask {

    @InputFile
    def source

    @OutputDirectory
    def targetRoot

    @Input
    def pathToParser

    @Input
    def pathToPsiRoot

    @OutputFile
    @Optional
    def parserFile = project.file("$targetRoot/$pathToParser")

    @OutputDirectory
    @Optional
    def psiDir = project.file("$targetRoot/$pathToPsiRoot")

    @Input
    @Optional
    def purgeOldFiles = false

    @TaskAction
    void generateParser() {
        def requiredLibs = [
                "jdom", "trove4j", "junit", "guava", "asm-all", "automaton", "platform-api", "platform-impl",
                "util", "annotations", "picocontainer", "extensions", "idea", "openapi", "Grammar-Kit",
                "platform-util-ui", "platform-concurrency", "intellij-deps-fastutil",
                // CLion unlike IDEA contains `MockProjectEx` in `testFramework.jar` instead of `idea.jar`
                // so this jar should be in `requiredLibs` list to avoid `NoClassDefFoundError` exception
                // while parser generation with CLion distribution
                "testFramework", "3rd-party"
        ]

        def clspth
        if (project.configurations.hasProperty("grammarKitClassPath")) {
            clspth = project.configurations.grammarKitClassPath
        } else {
            clspth = project.configurations.compileClasspath.files.findAll({
                for (lib in requiredLibs) {
                    if (it.name.equalsIgnoreCase("${lib}.jar") || it.name.startsWith("${lib}-")) {
                        return true;
                    }
                }
                return false;
            })
        }

        def os = new ByteArrayOutputStream()

        println "---------"
        println clspth
        println "---------"
        try {
            project.javaexec {
                mainClass = "org.intellij.grammar.Main"
                args = [project.file(targetRoot), source]
                classpath = project.files(clspth)
                standardOutput = os
            }
        } catch (Exception e) {
            throw new GradleException(os.toString().trim(), e)
        }

        println os.toString()
    }
}

