package org.jetbrains.grammarkit.tasks

class GenerateLexer extends BaseTask {

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
            def effectiveSkeleton = getEffectiveSkeleton()
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

            purgeFiles(targetFile)
        })
    }

    File getEffectiveSkeleton() {
        if (skeleton != null) {
            return project.file(skeleton);
        }
        def ext = getExtension()
        return ext.jflexSkeleton == null ? null : project.file(ext.jflexSkeleton)
    }
}
