package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.ImportThemeEvent
import io.github.frostzie.datapackide.settings.annotations.*
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty

object ThemeConfig {

    @Expose
    @ConfigCategory(name = "Theme")
    @ConfigOption(name = "Theme Selection", desc = "Select the application theme.")
    @ConfigEditorDropdown(
        values = [
            "Primer Light",
            "Primer Dark",
            "Nord Light",
            "Nord Dark",
            "Cupertino Light",
            "Cupertino Dark",
            "Dracula"
        ]
    )
    val theme = SimpleStringProperty("Primer Dark")

    @Expose
    @ConfigCategory(name = "Theme")
    @ConfigOption(name = "Font Size", desc = "Adjust the base font size for the UI. (Default: 13)")
    @ConfigEditorSpinner(minValue = 8, maxValue = 24)
    val fontSize = SimpleIntegerProperty(13)

    @Expose
    @ConfigCategory(name = "Custom Themes", desc = "Manage your custom themes.")
    @ConfigOption(name = "Import Custom Theme", desc = "Select a .css theme file to import and apply.")
    @ConfigEditorButton(text = "Import")
    val importTheme: () -> Unit = { EventBus.post(ImportThemeEvent()) }
}