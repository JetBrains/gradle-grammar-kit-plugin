package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.Input

class GenerateLexer extends BaseTask {

    @Input
    def targetDir
    @Input
    def targetClass
    @Input
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
            if (skeleton != null) {
                def skeletonFile = project.file(skeleton)
                newArgs.add("--skel")
                newArgs.add(skeletonFile)
                inputs.file skeletonFile
            }
            newArgs.add("-d")
            newArgs.add(project.file(targetDir))
            newArgs.add(flexFile)
            args(newArgs)

            classpath project.configurations.compileOnly

            purgeFiles(targetFile)
        })
    }
}
