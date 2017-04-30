package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.JavaExec

/**
 * Created by hurricup on 05.04.2017.
 */
abstract class BaseTask extends JavaExec {
    def purgeOldFiles = false

    protected void purgeFiles(File... files) {
        if (purgeOldFiles) {
            doFirst {
                project.delete(files)
            }
        }
    }
}
