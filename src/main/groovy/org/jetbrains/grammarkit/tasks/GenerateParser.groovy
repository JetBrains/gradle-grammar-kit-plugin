package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.Input

class GenerateParser extends BaseTask {
    @Input
    def source
    @Input
    def targetRoot
    // would be nice to obtain these from the GC
    @Input
    def pathToParser
    @Input
    def pathToPsiRoot

    GenerateParser() {
        setMain("org.intellij.grammar.Main");
        project.afterEvaluate {
            def bnfFile = project.file(source)
            inputs.file bnfFile
            def parserFile = project.file("$targetRoot/$pathToParser")
            outputs.file parserFile
            def psiDir = project.file("$targetRoot/$pathToPsiRoot")
            outputs.dir psiDir

            args = [project.file(targetRoot), bnfFile]

            def requiredLibs = [
                    "jdom", "trove4j", "junit", "guava", "asm-all", "automaton", "platform-api", "platform-impl",
                    "util", "annotations", "picocontainer", "extensions", "idea", "openapi", "Grammar-Kit",
                    "platform-util-ui", "platform-concurrency", "intellij-deps-fastutil",
                    // CLion unlike IDEA contains `MockProjectEx` in `testFramework.jar` instead of `idea.jar`
                    // so this jar should be in `requiredLibs` list to avoid `NoClassDefFoundError` exception
                    // while parser generation with CLion distribution
                    "testFramework", "3rd-party"
            ]

            classpath project.configurations.compileClasspath.files.findAll({
                for (lib in requiredLibs) {
                    if (it.name.equalsIgnoreCase("${lib}.jar") || it.name.startsWith("${lib}-")) {
                        return true;
                    }
                }
                return false;
            })

            purgeFiles(parserFile, psiDir)
        }
    }
}

