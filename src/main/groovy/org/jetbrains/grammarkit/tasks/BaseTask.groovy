package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

abstract class BaseTask extends JavaExec {
    @Input
    def purgeOldFiles = false

    @Internal
    @Override
    List<String> getJvmArgs() {
        return super.getJvmArgs()
    }

    protected void purgeFiles(File... files) {
        if (purgeOldFiles) {
            doFirst {
                project.delete(files)
            }
        }
    }
}
