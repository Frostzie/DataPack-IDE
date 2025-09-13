package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.KeyCombination
import io.github.frostzie.datapackide.settings.annotations.ConfigCategory
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorBoolean
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorDropdown
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorKeybind
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorText
import io.github.frostzie.datapackide.settings.annotations.ConfigOption
import io.github.frostzie.datapackide.settings.annotations.Expose
import javafx.scene.input.KeyCode


object MainConfig {

    @Expose
    @ConfigCategory(name = "Examples", desc = "Example settings")
    @ConfigOption(name = "True / False", desc = "Description")
    @ConfigEditorBoolean
    var example1: Boolean = true

    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Slider", desc = "Description")
    @ConfigEditorSlider(minValue = 5.0, maxValue = 300.0, stepSize = 5.0)
    var example2: Double = 30.0

    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Dropdown", desc = "Description")
    @ConfigEditorDropdown(values = ["Option 1", "Option 2", "Option 3", "Option 4"])
    var example3: String = "Option 3"

    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Text Area", desc = "Description")
    @ConfigEditorText
    var example4: String = "Example text..."
    
    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Keybind", desc = "Description")
    @ConfigEditorKeybind
    var example5: KeyCombination = KeyCombination(key = KeyCode.S, ctrl = true, alt = true)
}
