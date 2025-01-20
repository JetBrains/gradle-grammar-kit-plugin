# Gradle Grammar-Kit Plugin Changelog

## [Unreleased]

- Default Grammar-Kit updated to `2023.3` ([Changelog](https://github.com/JetBrains/Grammar-Kit/releases))

## [2022.3.2.2] - 2024-02-21

- Support for IntelliJ Platform `2024.1` — added `opentelementry` library.
- Fixes plugin for recent IntelliJ master where required libraries where changed
- Update JFlex dependency to `1.9.2` ([Changelog](https://github.com/JetBrains/intellij-deps-jflex/releases/tag/1.9.2))
- Default Grammar-Kit updated to `2022.3.2` ([Changelog](https://github.com/JetBrains/Grammar-Kit/releases))

## [2022.3.2.1] - 2023-12-22

- Fixes plugin for recent IntelliJ master where required libraries where changed

## [2022.3.2] - 2023-09-18

- Update JFlex dependency to `1.9.2` ([Changelog](https://github.com/JetBrains/intellij-deps-jflex/releases/tag/1.9.2))
- Default Grammar-Kit updated to `2022.3.2` ([Changelog](https://github.com/JetBrains/Grammar-Kit/releases))

## [2022.3.1] - 2023-02-23

- Breaking change: remove `generateParser.source` and `generateLexer.source` properties in favour of `generateParser.sourceFile` and `generateLexer.sourceFile`
- Set minimal supported Gradle to `7.4`
- Default Grammar-Kit updated to `2022.3.1`

## [2022.3]

- Default Grammar-Kit updated to `2022.3`
- Make `GenerateParserTask` and `GenerateLexerTask` subclasses of `JavaExec`
- Set minimum supported Gradle version from `6.7.1` to `7.0`
- Required Java version is `17`

## [2021.2.2]

- Remove redundant `bomConfiguration`
- Add `util_rt.jar` into GrammarKit call classpath

## [2021.2.1]

- Fixed resolving `requiredLibs` for the parser classpath
- Add `app.jar` to the classpath
- Fix for an issue when `compileClasspath` or `compileOnly` configurations are missing in the project.

## [2021.2]

- Default Grammar-Kit updated to 2021.1.2
- Introducing Gradle configuration cache, lazy properties
- Code refactoring, migration to Kotlin, tests

## [2021.1.3]

- Standalone usage by @AlecStrong
- Support for 2021.2 library layout by @mfilippov

## [2021.1.2]

- Should work fine with gradle 6 & 7. Build with Gradle 6.8.3 & Groovy 2.5.12 to avoid potential problems of new default method in Groovy 3.0.7

## [2021.1.1]

- More gradle 7.0 compatibility fixes

## [2021.1]

- Gradle 7.0 compatibility
- Bintray workaround

## [2020.3.2]

- Default Grammar-Kit updated to 2020.3.1

## [2020.3.1]

- Grammar-Kit updated to 2020.3
- Note: other revision used instead of release, because of JitPack fix

## [2020.2.1]

- Updated dependency library name

## [2020.2]

- Compatible with 2020.2

## [2020.1.4]

- Usable with Java 8

## [2020.1.3]

- Default Grammar-Kit is now 2020.1 (please, note, it requires jre11)

## [2020.1.2]

- skeleton is optional again

## [2020.1.1]

- Compatibility updates

## [2019.3]

- Default Grammar-Kit is now 2019.3

## [2019.2.1]

- Default Grammar-Kit is 2019.1.1

## [2019.2]

- Fixed dependencies for 2019.2 EAP snapshot

## [2019.1]

- Default Grammar-Kit is now 2019.1

## [2018.3.1]

- Classpath building is now more selective, and should not exceed Windows 32 kb limit

## [2018.3]

- Grammar-Kit 2017.1.7 is now default

## [2018.2.2]

- Excluded transient ant dependency for jflex task
- Both tasks using compileOnly configurations

## [2018.2.1]

- Grammar-Kit 2017.1.6 is now default

## [2018.2]

- Fixed problem with false positive upToDate while debugging because of jvmArgs

## [2018.1.7]

- Grammar-Kit 2017.1.5 is now default

## [2018.1.6]

- Published on gradle plugins

## [2018.1.3]

- Grammar-Kit 2017.1.4 is now default
- Minor fixes

## [2018.1.2]

- Default Grammar-Kit is 2017.1.3

## [2018.1.1]

- Default skeleton update

## [2018.1]

- Migrated to JetBrains GitHub account
- Using JFlex patched by JetBrains from the bintray
- Default Grammar-Kit is 2017.1.2

## [2017.1.1]

- Fixed bug with skeleton parameter

## [2017.1]

- First release

[Unreleased]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2022.3.2.2...HEAD
[2022.3.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2022.3.1...2022.3.2
[2022.3.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2022.3...2022.3.1
[2021.2.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.2.1...2021.2.2
[2021.2.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.2...2021.2.1
[2021.1.3]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.1.2...2021.1.3
[2021.1.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.1.1...2021.1.2
[2021.1.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.1...2021.1.1
[2020.3.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.3.1...2020.3.2
[2020.3.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.2.1...2020.3.1
[2020.2.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.2...2020.2.1
[2020.1.4]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.1.3...2020.1.4
[2020.1.3]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.1.2...2020.1.3
[2020.1.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.1.1...2020.1.2
[2020.1.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2019.3...2020.1.1
[2019.2.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2019.2...2019.2.1
[2018.3.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.3...2018.3.1
[2018.2.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.2.1...2018.2.2
[2018.2.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.2...2018.2.1
[2018.1.7]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.1.6...2018.1.7
[2018.1.6]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.1.3...2018.1.6
[2018.1.3]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.1.2...2018.1.3
[2018.1.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.1.1...2018.1.2
[2018.1.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.1...2018.1.1
[2017.1.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2017.1...2017.1.1
[2017.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/commits/2017.1
[2018.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2017.1.1...2018.1
[2018.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.1.7...2018.2
[2018.3]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.2.2...2018.3
[2019.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2018.3.1...2019.1
[2019.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2019.1...2019.2
[2019.3]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2019.2.1...2019.3
[2020.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.1.4...2020.2
[2021.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2020.3.2...2021.1
[2021.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.1.3...2021.2
[2022.3]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2021.2.2...2022.3
[2022.3.2.1]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2022.3.2...2022.3.2.1
[2022.3.2.2]: https://github.com/JetBrains/gradle-grammar-kit-plugin/compare/2022.3.2.1...2022.3.2.2
