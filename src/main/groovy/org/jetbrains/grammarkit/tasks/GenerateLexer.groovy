package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.JavaExec
import org.jetbrains.grammarkit.GrammarKitPluginExtension

class GenerateLexer extends JavaExec {

    def targetDir
    def targetClass
    def source
    def skeleton

    GenerateLexer() {
        project.afterEvaluate({
            setMain("jflex.Main")
            def flexFile = project.file(source)
            inputs.file flexFile
            def targetFile = project.file("${targetDir}/${targetClass}.java")
            outputs.file targetFile

            def newArgs = []
            def effectiveSkeleton = getSkeleton()
            if (effectiveSkeleton != null) {
                newArgs.add("--skel")
                newArgs.add(effectiveSkeleton)
                inputs.file effectiveSkeleton
            }
            newArgs.add("-d")
            newArgs.add(project.file(targetDir))
            newArgs.add(flexFile)
            args(newArgs)

            classpath project.configurations.jflex

            doFirst {
                project.delete targetFile
            }
        })
    }

    File getSkeleton() {
        if (skeleton != null) {
            return project.file(skeleton);
        }
        def ext = project.extensions.findByType(GrammarKitPluginExtension)
        return ext == null || ext.jflexSkeleton == null ? null : project.file(ext.jflexSkeleton)
    }
}
