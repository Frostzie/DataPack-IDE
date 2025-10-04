package io.github.frostzie.datapackide.utils.ui

import io.github.frostzie.datapackide.settings.KeyCombination
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.ui.controls.KeybindInputButton
import javafx.beans.property.Property
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/*
TODO: Refactor for full type-safety and removal of unchecked casts
- Roadmap:
- 1. Create a generic sealed interface, e.g., `TypedConfigField<T>`, with implementations for each setting type (BooleanField, StringField, etc.)
- 2. Update `SettingsManager.getConfigFields()` to use reflection on the property's return type to build and return a list of these specific `TypedConfigField` objects
- 3. Refactor `createControl()` to accept the sealed interface and use a `when` block to smart-cast, completely eliminating the need for unchecked casts (`as? ...`)
*/

/**
 * Builder for creating JavaFX controls for different setting types.
 */
object SettingsControlBuilder {
    private val logger = LoggerProvider.getLogger("SettingsControlBuilder")

    fun createControl(field: SettingsManager.ConfigField): Node {
        // Button is a special case as it's not a bindable property, but an action.
        if (field.editorType == SettingsManager.EditorType.BUTTON) {
            val buttonAnnotation = field.buttonAnnotation
            val action = field.property.get(field.objectInstance) as? () -> Unit

            return Button(buttonAnnotation?.text ?: field.name).apply {
                styleClass.add("field-button")
                setOnAction { _ ->
                    if (action != null) {
                        try {
                            action.invoke()
                            logger.debug("Action button '${field.name}' executed.")
                        } catch (e: Exception) {
                            logger.error("Error executing action for button '${field.name}'", e)
                        }
                    } else {
                        logger.warn("No action defined for button field '${field.name}'. Should be of type () -> Unit.")
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        val property = field.property.get(field.objectInstance) as? Property<Any>
        if (property == null) {
            return errorLabel(field.name, "Property<T>")
        }

        @Suppress("UNCHECKED_CAST")
        return when (field.editorType) {
            SettingsManager.EditorType.BOOLEAN -> {
                val prop = property as? Property<Boolean>
                    ?: return errorLabel(field.name, "Property<Boolean>")
                CheckBox().apply {
                    styleClass.add("field-checkbox")
                    selectedProperty().bindBidirectional(prop)
                }
            }

            SettingsManager.EditorType.TEXT -> {
                val prop = property as? Property<String>
                    ?: return errorLabel(field.name, "Property<String>")
                TextField().apply {
                    styleClass.add("field-textfield")
                    promptText = "Enter ${field.name.lowercase()}"
                    textProperty().bindBidirectional(prop)
                }
            }

            SettingsManager.EditorType.SLIDER -> {
                val prop = property as? Property<Number>
                    ?: return errorLabel(field.name, "Property<Number>")
                val sliderAnnotation = field.sliderAnnotation
                VBox().apply {
                    spacing = 5.0

                    val slider = Slider().apply {
                        styleClass.add("field-slider")
                        min = sliderAnnotation?.minValue ?: 0.0
                        max = sliderAnnotation?.maxValue ?: 100.0
                        blockIncrement = sliderAnnotation?.stepSize ?: 1.0
                        valueProperty().bindBidirectional(prop)
                    }

                    val valueLabel = Label().apply {
                        styleClass.add("slider-value")
                        text = formatSliderLabel(slider.value, sliderAnnotation)
                    }

                    slider.valueProperty().addListener { _, _, newValue ->
                        valueLabel.text = formatSliderLabel(newValue.toDouble(), sliderAnnotation)
                    }

                    children.addAll(slider, valueLabel)
                }
            }

            SettingsManager.EditorType.DROPDOWN -> {
                val prop = property as? Property<String>
                    ?: return errorLabel(field.name, "Property<String>")
                ComboBox<String>().apply {
                    styleClass.add("field-combobox")
                    val values = field.dropdownAnnotation?.values ?: emptyArray()
                    items.addAll(*values)
                    valueProperty().bindBidirectional(prop)
                }
            }

            SettingsManager.EditorType.KEYBIND -> {
                val prop = property as? Property<KeyCombination>
                    ?: return errorLabel(field.name, "Property<KeyCombination>")
                val keybindButton = KeybindInputButton().apply {
                    keybindProperty.bindBidirectional(prop)
                }

                val resetButton = Button("⟳").apply {
                    styleClass.add("keybind-reset-button")
                    setOnAction { _ ->
                        val defaultValue = SettingsManager.getDefaultValue(field.property) as? KeyCombination
                        defaultValue?.let {
                            property.value = it
                            logger.debug("Keybind field '{}' reset to default: {}", field.name, it)
                        }
                    }
                }

                HBox(5.0).apply {
                    children.addAll(keybindButton, resetButton)
                }
            }
            else -> errorLabel(field.name, "Unknown EditorType")
        }
    }

    private fun errorLabel(fieldName: String, expectedType: String): Label {
        val message = "Error for '$fieldName': Expected $expectedType"
        logger.error(message)
        return Label(message)
    }

    private fun formatSliderLabel(value: Double, annotation: ConfigEditorSlider?): String {
        val hasDecimals = annotation?.stepSize?.let { it < 1.0 } ?: false
        return if (hasDecimals) {
            "Value: %.2f".format(value)
        } else {
            "Value: %d".format(value.toInt())
        }
    }
}