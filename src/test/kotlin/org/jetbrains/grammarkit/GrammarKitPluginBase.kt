package org.jetbrains.grammarkit

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files.createTempDirectory
import java.util.zip.ZipFile
import kotlin.test.BeforeTest

abstract class GrammarKitPluginBase {

    private val gradleDefault = System.getProperty("test.gradle.default")
    private val gradleVersion = System.getProperty("test.gradle.version").takeUnless { it.isNullOrEmpty() } ?: gradleDefault
    private val gradleArguments = System.getProperty("test.gradle.arguments", "")
        .split(' ').filter(String::isNotEmpty).toTypedArray()

    val gradleHome: String = System.getProperty("test.gradle.home")

    val dir: File = createTempDirectory("tmp").toFile()

    val gradleProperties = file("gradle.properties")
    val buildFile = file("build.gradle")

    @BeforeTest
    open fun setUp() {
        file("settings.gradle").groovy("rootProject.name = 'projectName'")

        buildFile.groovy("""
            plugins {
                id 'java'
                id 'org.jetbrains.grammarkit'
            }
            sourceCompatibility = 1.8
            targetCompatibility = 1.8

            repositories {
                mavenCentral()
            }
        """)

        gradleProperties.groovy("""
            kotlin.stdlib.default.dependency = false
        """)
    }

    protected fun buildAndFail(vararg tasks: String): BuildResult = build(true, *tasks)

    protected fun build(vararg tasks: String): BuildResult = build(false, *tasks)

    protected fun build(fail: Boolean, vararg tasks: String): BuildResult = build(gradleVersion, fail, *tasks)

    protected fun build(gradleVersion: String, fail: Boolean = false, vararg tasks: String): BuildResult =
        builder(gradleVersion, *tasks).run {
            when (fail) {
                true -> buildAndFail()
                false -> build()
            }
        }

    private fun builder(gradleVersion: String, vararg tasks: String) =
        GradleRunner.create()
            .withProjectDir(dir)
            .withGradleVersion(gradleVersion)
            .forwardOutput()
            .withPluginClasspath()
            .withTestKitDir(File(gradleHome))
            .withArguments(*tasks, "--stacktrace", /*"--configuration-cache",*/ *gradleArguments)

    fun tasks(groupName: String): List<String> = build(ProjectInternal.TASKS_TASK).output.lines().run {
        val start = indexOfFirst { it.equals("$groupName tasks", ignoreCase = true) } + 2
        drop(start).takeWhile(String::isNotEmpty).map { it.substringBefore(' ') }
    }

    protected fun directory(path: String) = File(dir, path).apply { mkdirs() }

    protected fun file(path: String) = path
        .run { takeIf { startsWith('/') } ?: "${dir.path}/$this" }
        .split('/')
        .run { File(dropLast(1).joinToString("/")) to last() }
        .apply { if (!first.exists()) first.mkdirs() }
        .run { File(first, second) }
        .apply { createNewFile() }


    protected fun fileText(zipFile: ZipFile, path: String) = zipFile
        .getInputStream(zipFile.getEntry(path))
        .bufferedReader()
        .use(BufferedReader::readText)
        .replace("\r", "")
        .trim()

    // Methods can be simplified, when following tickets will be handled:
    // https://youtrack.jetbrains.com/issue/KT-24517
    // https://youtrack.jetbrains.com/issue/KTIJ-1001
    fun File.xml(@Language("XML") content: String) = append(content)

    fun File.groovy(@Language("Groovy") content: String) = append(content)

    fun File.java(@Language("Java") content: String) = append(content)

    fun File.kotlin(@Language("kotlin") content: String) = append(content)

    private fun File.append(content: String) = appendText(content.trimIndent() + "\n")

    protected fun getResourceFile(name: String) = javaClass.classLoader.getResource(name)?.file
}
