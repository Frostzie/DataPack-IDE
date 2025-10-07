package io.github.frostzie.datapackide.settings.data

import io.github.frostzie.datapackide.settings.KeyCombination
import io.github.frostzie.datapackide.settings.annotations.ConfigCategory
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorButton
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorDropdown
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import javafx.beans.property.Property
import kotlin.reflect.KProperty1

sealed interface ConfigField {
    val objectInstance: Any
    val property: KProperty1<Any, *>
    val name: String
    val description: String
    val editorType: EditorType
    val category: ConfigCategory?
}

data class BooleanConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<Boolean>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.BOOLEAN
}

data class TextConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.TEXT
}

data class SliderConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<Number>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val sliderAnnotation: ConfigEditorSlider
) : ConfigField {
    override val editorType: EditorType = EditorType.SLIDER
}

data class DropdownConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val dropdownAnnotation: ConfigEditorDropdown
) : ConfigField {
    override val editorType: EditorType = EditorType.DROPDOWN
}

data class KeybindConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<KeyCombination>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.KEYBIND
}

data class ButtonConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, () -> Unit>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val buttonAnnotation: ConfigEditorButton
) : ConfigField {
    override val editorType: EditorType = EditorType.BUTTON
}

enum class EditorType {
    BOOLEAN, TEXT, SLIDER, DROPDOWN, BUTTON, KEYBIND
}