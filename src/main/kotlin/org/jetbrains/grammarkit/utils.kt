// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

@file:JvmName("Utils")

package org.jetbrains.grammarkit

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

val <T : FileSystemLocation> Provider<T>.path: String
    get() = get().asFile.canonicalPath

// https://github.com/gradle/gradle/issues/17295#issuecomment-1053620508
val Project.settings: Settings get() = (gradle as GradleInternal).settings
