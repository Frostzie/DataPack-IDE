package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.RequestSettingsCategories
import javafx.scene.control.SplitPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * Main UI for the settings popup window.
 * This class is responsible for the overall layout, while delegating specific UI creation and logic.
 */
class SettingsView : VBox() {
    private val header = SettingsHeader()
    private val nav = SettingsNav()
    private val content = SettingsContent()
    private val footer = SettingsFooter()

    init {
        styleClass.add("settings-window")

        val mainContent = SplitPane().apply {
            styleClass.add("main-content")
            items.addAll(nav, content)
            setDividerPositions(0.25)
        }

        setVgrow(mainContent, Priority.ALWAYS)
        children.addAll(
            header,
            mainContent,
            footer
        )

        EventBus.post(RequestSettingsCategories())
    }
}