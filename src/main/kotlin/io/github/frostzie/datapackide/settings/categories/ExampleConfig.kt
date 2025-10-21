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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode

object ExampleConfig {

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "Boolean toggle for true or false decisions.", desc = "Description would show up here this could be very long or short too I will show off how long this can go by rambling about nothing so please enjoy my story about nothing: The story started with nothing the end. Thanks for the read! I hope you enjoyed the story of nothing!")
    @ConfigEditorBoolean
    val bool1 = SimpleBooleanProperty(true)

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "2nd Booolean this is veryyyyyy long so get ready to readddddd lol but not this won't be tooo long just tryyyy to gettttt to the neexxxttt line :)", desc = "Shorty")
    @ConfigEditorBoolean
    val bool2 = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "LOL000000000000000000000000000000000000000000000000000000000000000000000000000000fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", desc = "HALLOghhhhhhhhhhhhhhfhfhfhfghffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
    @ConfigEditorKeybind
    val key1 = SimpleObjectProperty(KeyCombination(key = KeyCode.S, ctrl = true, alt = true))

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "LOL", desc = "HALLO")
    @ConfigEditorKeybind
    val key2 = SimpleObjectProperty(KeyCombination(key = KeyCode.S, ctrl = true, alt = true))

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "DropdownLONGERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", desc = "DescriptionNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN")
    @ConfigEditorDropdown(values = ["Option 1", "Option 2", "Option 3", "Option 4"])
    val combo1 = SimpleStringProperty("Option 3")

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "Dropdown", desc = "Description is going to go somewhere around this area of my currently typed text!")
    @ConfigEditorDropdown(values = ["Option 1", "Option 2", "Option 3", "Option 4"])
    val combo2 = SimpleStringProperty("Option 3")

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "Text Areaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaiaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", desc = "Descriptioneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
    @ConfigEditorText
    val text1 = SimpleStringProperty("Example text...")

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "Text Area", desc = "Description")
    @ConfigEditorText
    val text2 = SimpleStringProperty("Example text...")

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "Sliderrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrreteererrrrrrrrrrrrrrrrrrrrrrrereeerrererererreeeeeeeeeeeeeeeeeeeeeeeeeererrrrrrrrrrrrrrrrrerrrrrrrrrrrrreeeeeeeeeeeeeeeeeeeeerrrrrrrrrrrrrrrrrrrr", desc = "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRiptionnnsssDescription")
    @ConfigEditorSlider(minValue = 5.0, maxValue = 300.0, stepSize = 5.0)
    val slider1 = SimpleDoubleProperty(30.0)

    @Expose
    @ConfigCategory(name = "Examples v2", desc = "These are the example settings that can be used!")
    @ConfigOption(name = "Slider", desc = "Description")
    @ConfigEditorSlider(minValue = 5.0, maxValue = 300.0, stepSize = 5.0)
    val slider2 = SimpleDoubleProperty(30.0)

}