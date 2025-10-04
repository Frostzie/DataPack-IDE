package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.KeyCombination
import io.github.frostzie.datapackide.settings.annotations.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode


object MainConfig {

    @Expose
    @ConfigCategory(name = "Examples", desc = "Example settings")
    @ConfigOption(name = "True / False", desc = "Description")
    @ConfigEditorBoolean
    val example1 = SimpleBooleanProperty(true)

    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Slider", desc = "Description")
    @ConfigEditorSlider(minValue = 5.0, maxValue = 300.0, stepSize = 5.0)
    val example2 = SimpleDoubleProperty(30.0)

    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Dropdown", desc = "Description")
    @ConfigEditorDropdown(values = ["Option 1", "Option 2", "Option 3", "Option 4"])
    val example3 = SimpleStringProperty("Option 3")

    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Text Area", desc = "Description")
    @ConfigEditorText
    val example4 = SimpleStringProperty("Example text...")
    
    @Expose
    @ConfigCategory(name = "Examples")
    @ConfigOption(name = "Keybind", desc = "Description")
    @ConfigEditorKeybind
    val example5 = SimpleObjectProperty(KeyCombination(key = KeyCode.S, ctrl = true, alt = true))
}
