package io.github.frostzie.datapackide.utils.ui

import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import io.github.frostzie.datapackide.settings.data.*
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.ui.controls.KeybindInputButton
import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * Builder for creating JavaFX controls for different setting types.
 */
object SettingsControlBuilder {
    private val logger = LoggerProvider.getLogger("SettingsControlBuilder")

    fun createControl(field: ConfigField): Node {
        val editorNode = createEditorNode(field)

        if (field is ButtonConfigField) {
            return editorNode
        }

        if (field is KeybindConfigField) {
            return editorNode
        }

        val resetButton = Button("⟳").apply {
            styleClass.add("setting-reset-button")
            tooltip = Tooltip("Reset to default")
            setOnAction { handleReset(field) }
        }

        return HBox(5.0).apply {
            alignment = Pos.CENTER_LEFT
            children.addAll(editorNode, resetButton)
            HBox.setHgrow(editorNode, Priority.ALWAYS)
        }
    }

    /**
     * Creates the specific input control for a given ConfigField.
     */
    private fun createEditorNode(field: ConfigField): Node {
        return when (field) {
            is ButtonConfigField -> {
                val action = field.property.get(field.objectInstance)
                Button(field.buttonAnnotation.text).apply {
                    styleClass.add("field-button")
                    setOnAction {
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
                VBox(5.0).apply {
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
                    items.addAll(*field.dropdownAnnotation.values)
                    valueProperty().bindBidirectional(prop)
                }
            }
            is KeybindConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val keybindButton = KeybindInputButton().apply {
                    keybindProperty.bindBidirectional(prop)
                }
                val resetButton = Button("⟳").apply {
                    styleClass.add("setting-reset-button")
                    tooltip = Tooltip("Reset to default")
                    setOnAction { handleReset(field) }
                }
                HBox(5.0).apply {
                    alignment = Pos.CENTER_LEFT
                    children.addAll(keybindButton, resetButton)
                }
            }
        }
    }

    /**
     * Resets a field to its default value using the SettingsManager.
     */
    private fun handleReset(field: ConfigField) {
        try {
            val defaultValue = SettingsManager.getDefaultValue(field.property)
            @Suppress("UNCHECKED_CAST")
            val prop = field.property.get(field.objectInstance) as Property<Any?>
            prop.value = defaultValue
            logger.debug("Field '{}' reset to default value: {}", field.name, defaultValue)
        } catch (e: Exception) {
            logger.error("Failed to reset field '${field.name}' to default.", e)
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