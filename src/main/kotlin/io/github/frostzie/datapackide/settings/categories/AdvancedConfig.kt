package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.annotations.ConfigCategory
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorButton
import io.github.frostzie.datapackide.settings.annotations.ConfigOption
import io.github.frostzie.datapackide.settings.annotations.Expose

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.UIAction
import io.github.frostzie.datapackide.events.UIActionEvent
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorBoolean

object AdvancedConfig {

    @Expose
    @ConfigCategory(name = "Dev", desc = "Debug and development options")
    @ConfigOption(name = "Reload Styles", desc = "Reloads all css styles from configs")
    @ConfigEditorButton(text = "Reload All Styles")
    val reloadStyles: () -> Unit = { EventBus.post(UIActionEvent(UIAction.RELOAD_STYLES)) }

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Reset All Styles", desc = "Resets all styles to their default state")
    @ConfigEditorButton(text = "Reset Styles")
    val resetStylesToDefault: () -> Unit = { EventBus.post(UIActionEvent(UIAction.RESET_STYLES_TO_DEFAULT)) }

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Enable Debug Mode", desc = "Enables all debugging features")
    @ConfigEditorBoolean
    var enableDebugMode: Boolean = false

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show TreeView Hitboxes", desc = "Shows visual borders around File Tree components for debugging layout")
    @ConfigEditorBoolean
    var debugTreeViewHitbox: Boolean = false
}