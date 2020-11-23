# gradle-grammarkit-plugin

This plugin simplifies automation of generating lexers and parsers for IntelliJ plugins.

NB: plugin does not support two-pass generation, therefore does not suport method mixins.

 
## Usage

### Loading and applying the plugin

```groovy
plugins {
    id "org.jetbrains.grammarkit" version "2020.3.2"
}

apply plugin: 'org.jetbrains.grammarkit'

// import is optional to make task creation easier
import org.jetbrains.grammarkit.tasks.*
```

### Configuration
Global configuration allows you to select necessary jFlex and Grammar-Kit versions.
```groovy
grammarKit {
    // version of IntelliJ patched JFlex (see bintray link below), Default is 1.7.0-1 
    jflexRelease = '1.7.0-1'

    // tag or short commit hash of Grammar-Kit to use (see link below). Default is 2020.3.1
    grammarKitRelease = '6452fde524'
}
```

## Tasks
### Generating lexer
```groovy
task generatePerlLexer(type: GenerateLexer) {
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
task generatePerl5Parser(type: GenerateParser) {
    // source bnf file
    source = "grammar/Perl5.bnf"
    
    // optional, task-specific root for the generated files. Default: none
    targetRoot = 'gen'
    
    // path to a parser file, relative to the targetRoot  
    pathToParser = '/com/perl5/lang/perl/parser/PerlParserGenerated.java'
    
    // path to a directory with generated psi files, relative to the targetRoot 
    pathToPsiRoot = '/com/perl5/lang/perl/psi'

    // if set, plugin will remove a parser output file and psi output directory before generating new ones. Default: false
    purgeOldFiles = true
}
```

## Links

* [IntelliJ-patched JFlex Sources](https://github.com/JetBrains/intellij-deps-jflex)
* [IntelliJ-patched JFlex Bintray](https://bintray.com/jetbrains/intellij-third-party-dependencies/jflex)
* [Grammar-Kit](https://github.com/JetBrains/Grammar-Kit)
* [Plugin page on plugin.gradle.org](https://plugins.gradle.org/plugin/org.jetbrains.grammarkit)

## Usage examples

* [Perl5 plugin](https://github.com/Camelcade/Perl5-IDEA/blob/master/build.gradle)
* [Rust plugin](https://github.com/intellij-rust/intellij-rust/blob/master/build.gradle.kts)
* [Bamboo Soy plugin](https://github.com/google/bamboo-soy/blob/master/build.gradle)

```
Copyright 2017-2018 org.jetbrains.intellij.plugins

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

```

