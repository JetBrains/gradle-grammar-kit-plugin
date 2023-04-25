// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import org.jetbrains.changelog.ChangelogSectionUrlBuilder
import org.jetbrains.dokka.gradle.DokkaTask

fun properties(key: String) = project.findProperty(key)?.toString()

plugins {
    `kotlin-dsl`
    `maven-publish`
    kotlin("jvm") version "1.8.21"
    id("com.gradle.plugin-publish") version "1.1.0"
    id("org.jetbrains.changelog") version "2.0.0"
    id("org.jetbrains.dokka") version "1.8.10"
}

version = properties("version")!!
group = properties("group")!!
description = properties("description")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website.set(properties("website"))
    vcsUrl.set(properties("vcsUrl"))

    plugins.create("grammarKitPlugin") {
        id = properties("pluginId")
        implementationClass = properties("pluginImplementationClass")
        displayName = properties("pluginDisplayName")
        description = properties("pluginDescription")
        tags.set(properties("tags")?.split(','))
    }
}

val dokkaHtml by tasks.getting(DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    archives(javadocJar)
    archives(sourcesJar)
}

changelog {
    headerParserRegex.set("""(\d+(\.\d+)+)""".toRegex())
    groups.set(emptyList())
    repositoryUrl.set(properties("website"))
    sectionUrlBuilder.set(ChangelogSectionUrlBuilder { repositoryUrl, currentVersion, previousVersion, isUnreleased ->
        repositoryUrl + when {
            isUnreleased -> when (previousVersion) {
                null -> "/commits"
                else -> "/compare/$previousVersion...HEAD"
            }

            previousVersion == null -> "/commits/$currentVersion"

            else -> "/compare/$previousVersion...$currentVersion"
        }
    })
}

tasks {
    test {
        val testGradleHomePath = "$buildDir/testGradleHome"
        doFirst {
            File(testGradleHomePath).mkdir()
        }
        systemProperties["test.gradle.home"] = testGradleHomePath
        systemProperties["test.gradle.default"] = properties("gradleVersion")
        systemProperties["test.gradle.version"] = properties("testGradleVersion")
        systemProperties["test.gradle.arguments"] = properties("testGradleArguments")
        outputs.dir(testGradleHomePath)
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
    }

    validatePlugins {
        enableStricterValidation.set(true)
    }
}
