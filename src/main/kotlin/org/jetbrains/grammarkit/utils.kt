// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

@file:JvmName("Utils")

package org.jetbrains.grammarkit

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.internal.GradleInternal
import org.gradle.api.provider.Provider
import java.nio.file.Path
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

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

/**
 * Retrieves the [Path] of the IntelliJ Platform with [Configurations.INTELLIJ_PLATFORM_DEPENDENCY] configuration.
 *
 * @receiver The [Configuration] to retrieve the product information from.
 * @return The [Path] of the IntelliJ Platform
 * @throws GradleException
 */
@Throws(GradleException::class)
internal fun FileCollection.platformPath() = with(toList()) {
    val message = when (size) {
        0 -> "No IntelliJ Platform dependency found."
        1 -> null
        else -> "More than one IntelliJ Platform dependencies found."
    } ?: return@with single().toPath().absolute().resolvePlatformPath()

    throw GradleException(
        """
        $message
        Please ensure there is a single IntelliJ Platform dependency defined in your project and that the necessary repositories, where it can be located, are added.
        See: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
        """.trimIndent(),
    )
}

internal fun Path.resolvePlatformPath() = deepResolve {
    when {
        // eliminate `/Application Name.app/Contents/...`
        name.endsWith(".app")
            -> this.listDirectoryEntries("Contents").firstOrNull()

        // set the root to the directory containing `product-info.json`
        listDirectoryEntries("product-info.json").isNotEmpty()
            -> this

        // set the root to the directory containing `Resources/product-info.json`
        listDirectoryEntries("Resources").firstOrNull()?.listDirectoryEntries("product-info.json").orEmpty()
            .isNotEmpty()
            -> this

        // stop when `lib/` is inside, even if it's a singleton
        listDirectoryEntries("lib").isNotEmpty()
            -> null

        else
            -> null
    }
}

private fun Path.deepResolve(block: Path.() -> Path?) = generateSequence(this) { parent ->
    val entry = parent
        .listDirectoryEntries()
        .singleOrNull{ !it.isHidden() } // pick an entry if it is a singleton in a directory, exclude hidden entries
        ?.takeIf { it.isDirectory() } // and this entry is a directory
        ?: return@generateSequence null
    block(entry)
}.last()
