// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import org.jetbrains.dokka.gradle.DokkaTask

fun properties(key: String) = project.findProperty(key)?.toString()

plugins {
    kotlin("jvm") version "1.7.0"
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.0.0-rc-3"
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jetbrains.dokka") version "1.7.10"
}

description = properties("description")
group = properties("group")!!
version = properties("version")!!

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

gradlePlugin {
    plugins.create("grammarKitPlugin") {
        id = properties("pluginId")
        implementationClass = properties("pluginImplementationClass")
        displayName = properties("pluginDisplayName")
        description = properties("pluginDescription")
    }
}

pluginBundle {
    website = properties("website")
    vcsUrl = properties("vcsUrl")
    description = properties("description")
    tags = properties("tags")?.split(',')
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
    headerParserRegex.set("""(\d+(\.\d+)+)""")
    groups.set(emptyList())
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
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
}
