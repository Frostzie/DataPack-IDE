package io.github.frostzie.datapackide.features

import io.github.frostzie.datapackide.features.editor.CaretColor
import io.github.frostzie.datapackide.features.editor.DirtyTabDecorator
import io.github.frostzie.datapackide.features.editor.EditorTabDecorator
import io.github.frostzie.datapackide.features.editor.FileIconDecorator
import io.github.frostzie.datapackide.styling.tabs.TabStyleManager
import io.github.frostzie.datapackide.styling.tabs.TabStyler
import io.github.frostzie.datapackide.styling.tabs.rules.ConfigRule
import io.github.frostzie.datapackide.styling.tabs.rules.DefaultRule
import io.github.frostzie.datapackide.styling.tabs.rules.DirtyStateRule
import io.github.frostzie.datapackide.styling.messages.MessageStyleManager
import io.github.frostzie.datapackide.styling.messages.rules.CustomIconRule
import io.github.frostzie.datapackide.styling.messages.rules.DefaultMessageRule
import io.github.frostzie.datapackide.styling.messages.rules.SeverityMessageRule

/**
 * A central registry for discovering and accessing all feature services.
 */
object FeatureRegistry {

    /**
     * A list of all services that can decorate an editor tab.
     * To add a new tab feature, create a class that implements [EditorTabDecorator]
     * and add an instance of it to this list.
     */
    //TODO: split up
    val editorTabDecorators: List<EditorTabDecorator> = listOf(
        TabStyler(),
        DirtyTabDecorator(),
        FileIconDecorator(),
        CaretColor()
    )

    init {
        //Register the style rules with the central TabStyleManager.
        TabStyleManager.registerRule(DefaultRule())
        TabStyleManager.registerRule(DirtyStateRule())
        TabStyleManager.registerRule(ConfigRule())

        // Register message style rules
        MessageStyleManager.registerRule(DefaultMessageRule())
        MessageStyleManager.registerRule(SeverityMessageRule())
        MessageStyleManager.registerRule(CustomIconRule())
    }
}
