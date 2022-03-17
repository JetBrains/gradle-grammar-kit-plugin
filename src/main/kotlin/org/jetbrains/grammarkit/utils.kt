@file:JvmName("Utils")

package org.jetbrains.grammarkit

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

val <T : FileSystemLocation> Provider<T>.path: String
    get() = get().asFile.canonicalPath
