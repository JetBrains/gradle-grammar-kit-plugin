package org.jetbrains.grammarkit.tasks

import org.gradle.api.tasks.JavaExec
import org.jetbrains.grammarkit.GrammarKitPluginExtension

/**
 * Created by hurricup on 05.04.2017.
 */
abstract class BaseTask extends JavaExec {
    def purgeOldFiles = false

    protected GrammarKitPluginExtension getExtension() {
        return project.extensions.findByType(GrammarKitPluginExtension)
    }

    protected boolean shouldPurge() {
        return purgeOldFiles || getExtension().purgeOldFiles;
    }

    protected void purgeFiles(File... files) {
        if (shouldPurge()) {
            doFirst {
                project.delete(files)
            }
        }
    }
}
