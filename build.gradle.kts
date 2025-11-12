// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.changelog.ChangelogSectionUrlBuilder
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.changelog)
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = properties("website")
    vcsUrl = properties("vcsUrl")

    plugins.create("grammarKitPlugin") {
        id = properties("pluginId").get()
        implementationClass = properties("pluginImplementationClass").get()
        displayName = properties("pluginDisplayName").get()
        description = properties("pluginDescription").get()
        tags = properties("tags").map { it.split(',') }
    }
}

val dokkaGeneratePublicationHtml by tasks.existing(DokkaGeneratePublicationTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaGeneratePublicationHtml)
    archiveClassifier = "javadoc"
    from(dokkaGeneratePublicationHtml.map { it.outputDirectory })
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

artifacts {
    archives(javadocJar)
    archives(sourcesJar)
}

changelog {
    headerParserRegex = """(\d+(\.\d+)+)""".toRegex()
    groups.empty()
    repositoryUrl = properties("website")
    sectionUrlBuilder = ChangelogSectionUrlBuilder { repositoryUrl, currentVersion, previousVersion, isUnreleased ->
        repositoryUrl + when {
            isUnreleased -> when (previousVersion) {
                null -> "/commits"
                else -> "/compare/$previousVersion...HEAD"
            }

            previousVersion == null -> "/commits/$currentVersion"

            else -> "/compare/$previousVersion...$currentVersion"
        }
    }
}

tasks {
    test {
        val testGradleHome = properties("testGradleUserHome")
            .map { File(it) }
            .getOrElse(
                layout.buildDirectory.asFile.map { it.resolve("testGradleHome") }.get()
            )

        systemProperties["test.gradle.home"] = testGradleHome
        systemProperties["test.gradle.default"] = properties("gradleVersion").orNull
        systemProperties["test.gradle.version"] = properties("testGradleVersion").orNull
        systemProperties["test.gradle.arguments"] = properties("testGradleArguments").orNull
        outputs.dir(testGradleHome)
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
    }

    validatePlugins {
        enableStricterValidation = true
    }
}
