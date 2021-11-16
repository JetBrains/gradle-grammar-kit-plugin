import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key)?.toString()

plugins {
    kotlin("jvm") version "1.6.0"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
    id("org.jetbrains.changelog") version "1.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

version = properties("version") ?: ""
group = properties("group") ?: ""
description = "This plugin allows you to generate lexers using JetBrains patched JFLex and parsers using Grammar-Kit."

gradlePlugin {
    plugins.create("grammarKitPlugin") {
        id = "org.jetbrains.grammarkit"
        displayName = "Gradle Grammar-Kit Plugin"
        implementationClass = "org.jetbrains.grammarkit.GrammarKitPlugin"
    }
}

pluginBundle {
    website = "https://github.com/JetBrains/gradle-grammar-kit-plugin"
    vcsUrl = "https://github.com/JetBrains/gradle-grammar-kit-plugin"
    description = "Plugin for generating lexers and parsers for IntelliJ plugins"
    tags = listOf("intellij", "jetbrains", "idea", "Grammar-Kit", "JFlex")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
        distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
    }

    test {
        configureTests(this)
    }
}

fun configureTests(testTask: Test) {
    val testGradleHomePath = "$buildDir/testGradleHome"
    testTask.doFirst {
        File(testGradleHomePath).mkdir()
    }
    testTask.systemProperties["test.gradle.home"] = testGradleHomePath
    testTask.systemProperties["test.gradle.default"] = properties("gradleVersion")
    testTask.systemProperties["test.gradle.version"] = properties("testGradleVersion")
    testTask.systemProperties["test.gradle.arguments"] = properties("testGradleArguments")
    testTask.outputs.dir(testGradleHomePath)
}
