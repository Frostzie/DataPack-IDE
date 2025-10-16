package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.ReloadTheme
import io.github.frostzie.datapackide.events.ResetDefaultTheme
import io.github.frostzie.datapackide.settings.annotations.*
import javafx.beans.property.SimpleBooleanProperty

object AdvancedConfig {

    @Expose
    @ConfigCategory(name = "Dev", desc = "Debug and development options")
    @ConfigOption(name = "Reload Styles", desc = "Reloads all css styles from configs")
    @ConfigEditorButton(text = "Reload All Styles")
    val reloadStyles: () -> Unit = { EventBus.post(ReloadTheme()) }

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Reset All Styles", desc = "Resets all styles to their default state")
    @ConfigEditorButton(text = "Reset Styles")
    val resetStylesToDefault: () -> Unit = { EventBus.post(ResetDefaultTheme()) }

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Enable Debug Mode", desc = "Enables all debugging features")
    @ConfigEditorBoolean
    val enableDebugMode = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show TreeView Hitboxes", desc = "Shows visual borders around File Tree components for debugging layout")
    @ConfigEditorBoolean
    val debugTreeViewHitbox = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show Layout Bounds", desc = "Shows visual borders around major UI components for debugging layout")
    @ConfigEditorBoolean
    val debugLayoutBounds = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show Resize Handles", desc = "Shows visual borders for window resize handles")
    @ConfigEditorBoolean
    val debugResizeHandles = SimpleBooleanProperty(false)
}