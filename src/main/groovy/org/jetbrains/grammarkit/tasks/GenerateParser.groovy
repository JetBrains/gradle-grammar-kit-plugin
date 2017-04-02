package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.JavaExec

class GenerateParser extends JavaExec {
    def source
    def targetRoot = "gen"
    // would be nice to obtain these from the GC
    def pathToParser
    def pathToPsiRoot

    GenerateParser() {
        setMain("org.intellij.grammar.Main");
        project.afterEvaluate {
            def bnfFile = project.file(source)
            inputs.file bnfFile
            def parserFile = project.file("$targetRoot$pathToParser")
            outputs.file parserFile
            def psiDir = project.file("$targetRoot$pathToPsiRoot")
            outputs.dir psiDir

            args = [project.file(targetRoot), bnfFile]

            classpath project.configurations.compile + project.configurations.compileOnly
            doFirst {
                project.delete parserFile
                project.delete psiDir
            }
        }
    }
}

