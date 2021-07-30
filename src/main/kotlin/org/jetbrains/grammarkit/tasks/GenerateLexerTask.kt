package org.jetbrains.grammarkit.tasks

import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class GenerateLexerTask extends ConventionTask {

    @OutputDirectory
    def targetDir

    @OutputFile
    def targetFile

    @Input
    def targetClass

    @InputFile
    def source

    @InputFile
    @Optional
    def skeleton

    @Input
    @Optional
    def purgeOldFiles = false

    @TaskAction
    void generateLexer() {
        def newArgs = []
        if (skeleton != null) {
            def skeletonFile = project.file(skeleton)
            newArgs.add("--skel")
            newArgs.add(skeletonFile)
        }
        newArgs.add("-d")
        newArgs.add(project.file(targetDir))

        def flexFile = project.file(source)
        newArgs.add(flexFile)

        def clspth
        if (project.configurations.hasProperty("grammarKitClassPath")) {
            clspth = project.configurations.grammarKitClassPath
        } else {
            clspth = project.configurations.compileClasspath.files.findAll(
                    { it.name.startsWith("jflex") })
        }

        def os = new ByteArrayOutputStream()

        try {
            project.javaexec {
                mainClass = "jflex.Main";
                args = newArgs
                classpath = project.files(clspth)
                standardOutput = os
            }
        } catch (Exception e) {
            throw new GradleException(os.toString().trim(), e)
        }

        println os.toString()
    }
}
