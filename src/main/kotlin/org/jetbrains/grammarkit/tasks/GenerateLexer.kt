package org.jetbrains.grammarkit.tasks

import org.gradle.api.model.ObjectFactory

/**
 * @deprecated Use {@link GenerateLexerTask}.
 */
@Deprecated("Use GenerateLexerTask")
class GenerateLexer(objectFactory: ObjectFactory) : GenerateLexerTask(objectFactory)
