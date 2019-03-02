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
                    "util", "annotations", "picocontainer", "extensions", "idea", "openapi", "Grammar-Kit"
            ]

            classpath project.configurations.compileOnly.files.findAll({
                for(lib in requiredLibs){
                    if(it.name.equalsIgnoreCase("${lib}.jar") || it.name.startsWith("${lib}-")){
                        return true;
                    }
                }
                return false;
            })

            purgeFiles(parserFile, psiDir)
        }
    }
}

