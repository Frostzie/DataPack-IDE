package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.RequestSettingsCategories
import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import javafx.beans.value.ChangeListener
import javafx.scene.control.SplitPane

/**
 * The main content view for the settings window, containing the navigation and content areas.
 */
class SettingsView : SplitPane() {
    private val debugLayoutListener = ChangeListener { _, _, newValue ->
        if (newValue) {
            styleClass.add("debug-layout")
        } else {
            styleClass.remove("debug-layout")
        }
    }

    init {
        val nav = SettingsNav()
        val content = SettingsContent()

        styleClass.add("main-content")
        items.addAll(nav, content)
        setDividerPositions(0.25)

        AdvancedConfig.debugLayoutBounds.addListener(debugLayoutListener)
        if (AdvancedConfig.debugLayoutBounds.get()) {
            styleClass.add("debug-layout")
        }

        EventBus.post(RequestSettingsCategories())
    }

    fun dispose() {
        AdvancedConfig.debugLayoutBounds.removeListener(debugLayoutListener)
    }
}