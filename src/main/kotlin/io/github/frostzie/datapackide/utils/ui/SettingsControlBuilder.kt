package io.github.frostzie.datapackide.utils.ui

import io.github.frostzie.datapackide.settings.*
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.ui.controls.KeybindInputButton
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * Builder for creating JavaFX controls for different setting types.
 */
object SettingsControlBuilder {
    private val logger = LoggerProvider.getLogger("SettingsControlBuilder")

    fun createControl(field: ConfigField): Node {
        return when (field) {
            is ButtonConfigField -> {
                val action = field.property.get(field.objectInstance)
                Button(field.buttonAnnotation.text).apply {
                    styleClass.add("field-button")
                    setOnAction { _ ->
                        try {
                            action.invoke()
                            logger.debug("Action button '${field.name}' executed.")
                        } catch (e: Exception) {
                            logger.error("Error executing action for button '${field.name}'", e)
                        }
                    }
                }
            }
            is BooleanConfigField -> {
                val prop = field.property.get(field.objectInstance)
                CheckBox().apply {
                    styleClass.add("field-checkbox")
                    selectedProperty().bindBidirectional(prop)
                }
            }
            is TextConfigField -> {
                val prop = field.property.get(field.objectInstance)
                TextField().apply {
                    styleClass.add("field-textfield")
                    promptText = "Enter ${field.name.lowercase()}"
                    textProperty().bindBidirectional(prop)
                }
            }
            is SliderConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val sliderAnnotation = field.sliderAnnotation
                VBox().apply {
                    spacing = 5.0

                    val slider = Slider().apply {
                        styleClass.add("field-slider")
                        min = sliderAnnotation.minValue
                        max = sliderAnnotation.maxValue
                        blockIncrement = sliderAnnotation.stepSize
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
            is DropdownConfigField -> {
                val prop = field.property.get(field.objectInstance)
                ComboBox<String>().apply {
                    styleClass.add("field-combobox")
                    val values = field.dropdownAnnotation.values
                    items.addAll(*values)
                    valueProperty().bindBidirectional(prop)
                }
            }
            is KeybindConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val keybindButton = KeybindInputButton().apply {
                    keybindProperty.bindBidirectional(prop)
                }

                val resetButton = Button("⟳").apply {
                    styleClass.add("keybind-reset-button")
                    setOnAction { _ ->
                        val defaultValue = SettingsManager.getDefaultValue(field.property) as? KeyCombination
                        defaultValue?.let {
                            prop.value = it
                            logger.debug("Keybind field '{}' reset to default: {}", field.name, it)
                        }
                    }
                }

                HBox(5.0).apply {
                    children.addAll(keybindButton, resetButton)
                }
            }
        }
    }

    private fun formatSliderLabel(value: Double, annotation: ConfigEditorSlider): String {
        val hasDecimals = annotation.stepSize < 1.0
        return if (hasDecimals) {
            "Value: %.2f".format(value)
        } else {
            "Value: %d".format(value.toInt())
        }
    }
}