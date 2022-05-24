import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key)?.toString()

plugins {
    kotlin("jvm") version "1.6.21"
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.0.0-rc-2"
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jetbrains.dokka") version "1.6.21"
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

changelog {
    headerParserRegex.set("""(\d+(\.\d+)+)""")
    groups.set(emptyList())
}

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

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
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
