package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.KeyCombination
import io.github.frostzie.datapackide.settings.annotations.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode

object ExampleConfig {

    @Expose
    @ConfigCategory(name = "Example", desc = "Example description")
    @ConfigOption(name = "Info", desc = "Pray observe, dear user, this most distinguished informational tile. Though it performs no remarkable feats nor commands the slightest hint of wizardry, it stands proudly as a beacon of gentle guidance. One might employ this panel to impart notes, advisories, or particularly polite warnings to future travellers of the settings menu. Should you choose to bestow upon it a purpose of genuine importance, it shall accept such responsibility with quiet dignity. Until then, let it loiter here in graceful idleness, murmuring softly of its own exemplary usefulness.")
    @ConfigEditorInfo
    val info = ""

    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Boolean", desc = "Description")
    @ConfigEditorBoolean
    val bool = SimpleBooleanProperty(true)

    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Keybinds", desc = "Description")
    @ConfigEditorKeybind
    val key = SimpleObjectProperty(KeyCombination(key = KeyCode.S, ctrl = true, alt = true))

    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Dropdown", desc = "Description")
    @ConfigEditorDropdown(values = ["Option 1", "Option 2", "Option 3", "Option 4"])
    val combo = SimpleStringProperty("Option 3")
    
    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Text field", desc = "Description")
    @ConfigEditorText
    val text = SimpleStringProperty("Example text...")

    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Slider", desc = "Description")
    @ConfigEditorSlider(minValue = 5.0, maxValue = 300.0, stepSize = 5.0)
    val slider = SimpleDoubleProperty(30.0)

    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Spinner", desc = "Description")
    @ConfigEditorSpinner(minValue = 1, maxValue = 10)
    val spinner = SimpleIntegerProperty(5)

    @Expose
    @ConfigCategory(name = "Example")
    @ConfigOption(name = "Button", desc = "Description")
    @ConfigEditorButton(text = "Button")
    val button: () -> Unit = {}
}