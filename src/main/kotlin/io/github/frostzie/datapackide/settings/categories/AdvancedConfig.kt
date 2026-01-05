package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.settings.annotations.*
import javafx.beans.property.SimpleBooleanProperty

object AdvancedConfig {

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Reload Theme", desc = "Reloads theme file")
    @ConfigEditorButton(text = "Reload All Styles")
    val reloadStyles: () -> Unit = { EventBus.post(ReloadThemeEvent()) }

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Open Project Manager", desc = "Closes the current project and returns to the start screen")
    @ConfigEditorButton(text = "Open Project Manager")
    val openProjectManager: () -> Unit = { EventBus.post(OpenProjectManagerEvent()) }

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Reset Workspace", desc = "Resets ALL workspace history and settings, and returns to start screen")
    @ConfigEditorButton(text = "Reset Workspace")
    val resetWorkspace: () -> Unit = { EventBus.post(ResetWorkspaceEvent()) }

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