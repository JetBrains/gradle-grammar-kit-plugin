// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.grammarkit

// update docs for GrammarKitPluginExtension default values
object GrammarKitConstants {
    const val GROUP_NAME = "grammarKit"
    const val GENERATE_LEXER_TASK_NAME = "generateLexer"
    const val GENERATE_LEXER_OUT_DIR = "generated/sources/grammarkit-lexer/java/main"
    const val GENERATE_PARSER_TASK_NAME = "generateParser"
    const val GENERATE_PARSER_OUT_DIR = "generated/sources/grammarkit-parser/java/main"
    const val GRAMMAR_KIT_DEFAULT_VERSION = "2022.3.2"
    const val JFLEX_DEFAULT_VERSION = "1.9.2"
    const val MINIMAL_SUPPORTED_GRADLE_VERSION = "7.4"
    const val GRAMMAR_KIT_CLASS_PATH_CONFIGURATION_NAME = "grammarKitClassPath"
}
