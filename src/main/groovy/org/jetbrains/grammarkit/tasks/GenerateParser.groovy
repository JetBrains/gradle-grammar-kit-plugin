package org.jetbrains.grammarkit.tasks

class GenerateParser extends BaseTask {
    def source
    def targetRoot
    // would be nice to obtain these from the GC
    def pathToParser
    def pathToPsiRoot

    GenerateParser() {
        setMain("org.intellij.grammar.Main");
        project.afterEvaluate {
            def bnfFile = project.file(source)
            inputs.file bnfFile
            def effectiveTargetRoot = getEffectiveTargetRoot();
            def parserFile = project.file("$effectiveTargetRoot$pathToParser")
            outputs.file parserFile
            def psiDir = project.file("$effectiveTargetRoot$pathToPsiRoot")
            outputs.dir psiDir

            args = [project.file(effectiveTargetRoot), bnfFile]

            classpath project.configurations.compile + project.configurations.compileOnly

            purgeFiles(parserFile, psiDir)
        }
    }

    private String getEffectiveTargetRoot() {
        if (targetRoot != null) {
            return targetRoot;
        }
        return getExtension().grammarKitTargetRoot;
    }
}

