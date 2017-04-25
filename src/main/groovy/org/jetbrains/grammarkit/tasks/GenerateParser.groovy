package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class GenerateParser extends BaseTask {
    @Input def source
    @Input @Optional def targetRoot
    // would be nice to obtain these from the GC
    @Input def pathToParser
    @Input def pathToPsiRoot

    GenerateParser() {
        setMain("org.intellij.grammar.Main");
        project.afterEvaluate {
            def bnfFile = project.file(source)
            inputs.file bnfFile
            def effectiveTargetRoot = getEffectiveTargetRoot()
            def parserFile = project.file("$effectiveTargetRoot$pathToParser")
            outputs.file parserFile
            def psiDir = project.file("$effectiveTargetRoot$pathToPsiRoot")
            outputs.dir psiDir

            args = [project.file(effectiveTargetRoot), bnfFile]

            classpath project.configurations.compile + project.configurations.compileOnly

            purgeFiles(parserFile, psiDir)
        }
    }

    String getEffectiveTargetRoot() {
        if (targetRoot != null) {
            return targetRoot;
        }
        return getExtension().grammarKitTargetRoot;
    }
}

