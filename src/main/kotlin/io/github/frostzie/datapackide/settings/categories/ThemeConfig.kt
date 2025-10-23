package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.annotations.ConfigCategory
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorDropdown
import io.github.frostzie.datapackide.settings.annotations.ConfigOption
import io.github.frostzie.datapackide.settings.annotations.Expose
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
}