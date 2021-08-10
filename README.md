[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:confluence-on-gh]
[![Twitter Follow](https://img.shields.io/twitter/follow/JBPlatform?style=flat)][jb:twitter]
[![Build](https://github.com/JetBrains/gradle-grammar-kit-plugin/workflows/Build/badge.svg)][gh:build]
[![Slack](https://img.shields.io/badge/Slack-%23intellij--platform-blue)][jb:slack]

# gradle-grammarkit-plugin

**This project requires Gradle 6.6 or newer**

This plugin simplifies the automation of generating lexers and parsers for IntelliJ plugins.

NB: the plugin does not support two-pass generation. Therefore, it does not support method mixins.

## Usage

### Loading and applying the plugin

```groovy
plugins {
    id "org.jetbrains.grammarkit" version "2021.1.3"
}
```

### Configuration

Global configuration allows you to select necessary jFlex and Grammar-Kit versions.

```groovy
grammarKit {
    // version of IntelliJ patched JFlex (see the link below), Default is 1.7.0-1 
    jflexRelease = '1.7.0-1'

    // tag or short commit hash of Grammar-Kit to use (see link below). Default is 2020.3.1
    grammarKitRelease = '6452fde524'
  
    // optionally provide an IntelliJ version to build the classpath for GenerateParser/GenerateLexer tasks
    intellijRelease = '203.7717.81'
}
```

## Tasks

### Generating lexer

```groovy
generateLexer {
    // source flex file
    source = "grammar/Perl.flex"
    
    // target directory for lexer
    targetDir = "gen/com/perl5/lang/perl/lexer/"
    
    // target classname, target file will be targetDir/targetClass.java
    targetClass = "PerlLexer"
    
    // optional, path to the task-specific skeleton file. Default: none
    skeleton = '/some/specific/skeleton'
    
    // if set, plugin will remove a lexer output file before generating new one. Default: false
    purgeOldFiles = true
}
```

### Generating parser

```groovy
generateParser {
    // source bnf file
    source = "grammar/Perl5.bnf"

    // optional, task-specific root for the generated files. Default: none
    targetRoot = 'gen'

    // path to a parser file, relative to the targetRoot  
    pathToParser = '/com/perl5/lang/perl/parser/PerlParserGenerated.java'

    // path to a directory with generated psi files, relative to the targetRoot 
    pathToPsiRoot = '/com/perl5/lang/perl/psi'

    // if set, the plugin will remove a parser output file and psi output directory before generating new ones. Default: false
    purgeOldFiles = true
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
