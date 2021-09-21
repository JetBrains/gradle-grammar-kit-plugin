[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:confluence-on-gh]
[![Twitter Follow](https://img.shields.io/twitter/follow/JBPlatform?style=flat-square&logo=twitter)][jb:twitter]
[![Build](https://github.com/JetBrains/gradle-grammar-kit-plugin/workflows/Build/badge.svg)][gh:build]
[![Slack](https://img.shields.io/badge/Slack-%23intellij--platform-blue?style=flat-square&logo=Slack)][jb:slack]

# gradle-grammarkit-plugin

> **Important:**
> This project requires Gradle 6.6 or newer, however it is recommended to use the [latest Gradle available](https://gradle.org/releases/). Update it with:
> ```bash
> ./gradlew wrapper --gradle-version=VERSION
> ```

This Gradle plugin automates generating lexers and parsers to support custom language development in IntelliJ plugins when using [Grammar-Kit](https://github.com/JetBrains/Grammar-Kit).

NB: The plugin does not support two-pass generation. Therefore, it does not support method mixins.

## Usage

### Loading and applying the plugin

**Groovy** – `build.gradle`
```groovy
plugins {
    id "org.jetbrains.grammarkit" version "..."
}
```

**Kotlin DSL** – `build.gradle.kts`
```kotlin
plugins {
    id("org.jetbrains.grammarkit") version "..."
}
```

> **Note:** The latest version is: [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/org.jetbrains.grammarkit?color=green&label=Gradle%20Plugin%20Portal&logo=gradle)][gradle-plugin-page]

### Configuration

Global configuration allows you to select necessary jFlex and Grammar-Kit versions.

**Groovy** – `build.gradle`
```groovy
grammarKit {
  // Version of IntelliJ patched JFlex (see the link below), Default is 1.7.0-1 
  jflexRelease = "1.7.0-1"

  // Release version of Grammar-Kit to use (see link below). By default, the latest available is used.
  grammarKitRelease = "2021.1.1"
  
  // Optionally provide an IntelliJ version to build the classpath for GenerateParser/GenerateLexer tasks
  intellijRelease = "203.7717.81"
}
```

**Kotlin DSL** – `build.gradle.kts`
```kotlin
plugins {
  // Version of IntelliJ patched JFlex (see the link below), Default is 1.7.0-1 
  jflexRelease.set("1.7.0-1")

  // Release version of Grammar-Kit to use (see link below). By default, the latest available is used.
  grammarKitRelease.set("2021.1.1")

  // Optionally provide an IntelliJ version to build the classpath for GenerateParser/GenerateLexer tasks
  intellijRelease.set("203.7717.81")
}
```

## Tasks

### Generating lexer

**Groovy** – `build.gradle`
```groovy
generateLexer {
    // source flex file
    source = "grammar/Perl.flex"
    
    // target directory for lexer
    targetDir = "gen/com/perl5/lang/perl/lexer/"
    
    // target classname, target file will be targetDir/targetClass.java
    targetClass = "PerlLexer"
    
    // optional, path to the task-specific skeleton file. Default: none
    skeleton = "/some/specific/skeleton"
    
    // if set, plugin will remove a lexer output file before generating new one. Default: false
    purgeOldFiles = true
}
```

**Kotlin DSL** – `build.gradle.kts`
```kotlin
generateLexer {
    // source flex file
    source.set("grammar/Perl.flex")
    
    // target directory for lexer
    targetDir.set("gen/com/perl5/lang/perl/lexer/")
    
    // target classname, target file will be targetDir/targetClass.java
    targetClass.set("PerlLexer")
    
    // optional, path to the task-specific skeleton file. Default: none
    skeleton.set("/some/specific/skeleton")
    
    // if set, plugin will remove a lexer output file before generating new one. Default: false
    purgeOldFiles.set(true)
}
```

### Generating parser

**Groovy** – `build.gradle`
```groovy
generateParser {
    // source bnf file
    source = "grammar/Perl5.bnf"

    // optional, task-specific root for the generated files. Default: none
    targetRoot = "gen"

    // path to a parser file, relative to the targetRoot  
    pathToParser = "/com/perl5/lang/perl/parser/PerlParserGenerated.java"

    // path to a directory with generated psi files, relative to the targetRoot 
    pathToPsiRoot = "/com/perl5/lang/perl/psi"

    // if set, the plugin will remove a parser output file and psi output directory before generating new ones. Default: false
    purgeOldFiles = true
}
```

**Kotlin DSL** – `build.gradle.kts`
```kotlin
generateParser {
    // source bnf file
    source.set("grammar/Perl5.bnf")

    // optional, task-specific root for the generated files. Default: none
    targetRoot.set("gen")

    // path to a parser file, relative to the targetRoot  
    pathToParser.set("/com/perl5/lang/perl/parser/PerlParserGenerated.java")

    // path to a directory with generated psi files, relative to the targetRoot 
    pathToPsiRoot.set("/com/perl5/lang/perl/psi")

    // if set, the plugin will remove a parser output file and psi output directory before generating new ones. Default: false
    purgeOldFiles.set(true)
}
```

## Links

* [IntelliJ-patched JFlex Sources](https://github.com/JetBrains/intellij-deps-jflex)
* [IntelliJ-patched JFlex](https://cache-redirector.jetbrains.com/intellij-dependencies/org/jetbrains/intellij/deps/jflex/jflex/)
* [Grammar-Kit](https://github.com/JetBrains/Grammar-Kit)
* [Plugin page on plugin.gradle.org](https://plugins.gradle.org/plugin/org.jetbrains.grammarkit)

## Usage examples

* [Perl5 plugin](https://github.com/Camelcade/Perl5-IDEA/blob/master/build.gradle)
* [Rust plugin](https://github.com/intellij-rust/intellij-rust/blob/master/build.gradle.kts)
* [Bamboo Soy plugin](https://github.com/google/bamboo-soy/blob/master/build.gradle)


[gh:build]: https://github.com/JetBrains/gradle-grammar-kit-plugin/actions?query=workflow%3ABuild
[jb:confluence-on-gh]: https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub
[jb:slack]: https://plugins.jetbrains.com/slack
[jb:twitter]: https://twitter.com/JBPlatform
[gradle-plugin-page]: https://plugins.gradle.org/plugin/org.jetbrains.grammarkit
