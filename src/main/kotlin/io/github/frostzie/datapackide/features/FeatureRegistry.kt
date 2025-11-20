package io.github.frostzie.datapackide.features

import io.github.frostzie.datapackide.features.editor.DirtyTabDecorator
import io.github.frostzie.datapackide.features.editor.DirtyTextColorDecorator
import io.github.frostzie.datapackide.features.editor.EditorTabDecorator
import io.github.frostzie.datapackide.features.editor.FileIconDecorator

/**
 * A central registry for discovering and accessing all feature services.
 */
object FeatureRegistry {

    /**
     * A list of all services that can decorate an editor tab.
     * To add a new tab feature, create a class that implements [EditorTabDecorator]
     * and add an instance of it to this list.
     */
    val editorTabDecorators: List<EditorTabDecorator> = listOf(
        DirtyTabDecorator(),
        DirtyTextColorDecorator(),
        FileIconDecorator()
    )
}
