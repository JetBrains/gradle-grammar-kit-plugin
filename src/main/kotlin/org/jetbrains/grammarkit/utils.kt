// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

@file:JvmName("Utils")

package org.jetbrains.grammarkit

import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.internal.GradleInternal
import org.gradle.api.provider.Provider
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

internal val <T : FileSystemLocation> Provider<T>.path
    get() = get().asFile.canonicalPath

// https://github.com/gradle/gradle/issues/17295#issuecomment-1053620508
internal val Project.settings get() = (gradle as GradleInternal).settings

@FunctionalInterface
internal fun interface TriFunction<A, B, C, R> {
    fun apply(a: A, b: B, c: C): R

    fun <V> andThen(after: Function<in R, out V>): TriFunction<A, B, C, V> {
        Objects.requireNonNull(after)
        return TriFunction { a: A, b: B, c: C -> after.apply(apply(a, b, c)) }
    }
}
