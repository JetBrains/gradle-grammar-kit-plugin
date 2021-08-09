@file:JvmName("Utils")

package org.jetbrains.grammarkit

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty

val <T : FileSystemLocation> FileSystemLocationProperty<T>.path: String
    get() = asFile.get().canonicalPath
