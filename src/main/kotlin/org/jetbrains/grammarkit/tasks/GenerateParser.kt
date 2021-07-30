package org.jetbrains.grammarkit.tasks

import org.gradle.api.model.ObjectFactory

/**
 * @deprecated Use {@link GenerateParserTask}
 */
@Deprecated("Use GenerateParserTask")
class GenerateParser(objectFactory: ObjectFactory) : GenerateParserTask(objectFactory)
