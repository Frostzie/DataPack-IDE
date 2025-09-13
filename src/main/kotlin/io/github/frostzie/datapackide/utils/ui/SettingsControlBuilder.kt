package io.github.frostzie.datapackide.utils.ui

import io.github.frostzie.datapackide.settings.KeyCombination
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.ui.controls.KeybindInputButton
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

/**
 * Builder for creating JavaFX controls for different setting types.
 */
object SettingsControlBuilder {

    private val logger = LoggerProvider.getLogger("SettingsControlBuilder")

    fun createControl(field: SettingsManager.ConfigField): Node {
        val mutableProperty = field.objectInstance::class.memberProperties
            .filterIsInstance<KMutableProperty1<Any, Any>>()
            .find { it.name == field.property.name }

        val initialValue = field.property.get(field.objectInstance)

        return when (field.editorType) {
            SettingsManager.EditorType.BOOLEAN -> {
                CheckBox().apply {
                    styleClass.add("field-checkbox")
                    isSelected = initialValue as? Boolean ?: false
                    setOnAction {
                        try {
                            mutableProperty?.setter?.call(field.objectInstance, isSelected)
                            logger.debug("Boolean field '${field.name}' changed to: $isSelected")
                        } catch (e: Exception) {
                            logger.error("Failed to set boolean property ${field.name}", e)
                        }
                    }
                }
            }

            SettingsManager.EditorType.TEXT -> {
                TextField().apply {
                    styleClass.add("field-textfield")
                    promptText = "Enter ${field.name.lowercase()}"
                    text = initialValue as? String ?: ""
                    textProperty().addListener { _, _, newValue ->
                        try {
                            mutableProperty?.setter?.call(field.objectInstance, newValue)
                            logger.debug("Text field '${field.name}' changed to: $newValue")
                        } catch (e: Exception) {
                            logger.error("Failed to set text property ${field.name}", e)
                        }
                    }
                }
            }

            SettingsManager.EditorType.SLIDER -> {
                val sliderAnnotation = field.sliderAnnotation
                VBox().apply {
                    spacing = 5.0

                    val slider = Slider().apply {
                        styleClass.add("field-slider")
                        min = sliderAnnotation?.minValue ?: 0.0
                        max = sliderAnnotation?.maxValue ?: 100.0
                        value = (initialValue as? Number)?.toDouble() ?: min
                        blockIncrement = sliderAnnotation?.stepSize ?: 1.0
                    }

                    val valueLabel = Label(formatSliderLabel(slider.value, sliderAnnotation)).apply {
                        styleClass.add("slider-value")
                    }

                    slider.valueProperty().addListener { _, _, newValue ->
                        valueLabel.text = formatSliderLabel(newValue.toDouble(), sliderAnnotation)
                    }

                    slider.valueChangingProperty().addListener { _, _, isChanging ->
                        if (!isChanging) {
                            try {
                                mutableProperty?.setter?.call(field.objectInstance, slider.value)
                                logger.debug("Slider field '${field.name}' changed to: ${slider.value}")
                            } catch (e: Exception) {
                                logger.error("Failed to set slider property ${field.name}", e)
                            }
                        }
                    }

                    children.addAll(slider, valueLabel)
                }
            }

            SettingsManager.EditorType.DROPDOWN -> {
                ComboBox<String>().apply {
                    styleClass.add("field-combobox")
                    val values = field.dropdownAnnotation?.values ?: emptyArray()
                    items.addAll(*values)
                    value = initialValue as? String

                    selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        if (newValue != null) {
                            try {
                                mutableProperty?.setter?.call(field.objectInstance, newValue)
                                logger.debug("Dropdown field '${field.name}' changed to: $newValue")
                            } catch (e: Exception) {
                                logger.error("Failed to set dropdown property ${field.name}", e)
                            }
                        }
                    }
                }
            }

            SettingsManager.EditorType.BUTTON -> {
                val buttonAnnotation = field.buttonAnnotation
                val action = field.property.get(field.objectInstance) as? () -> Unit

                Button(buttonAnnotation?.text ?: field.name).apply {
                    styleClass.add("field-button")
                    setOnAction {
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

            SettingsManager.EditorType.KEYBIND -> {
                val initialKeybind = initialValue as? KeyCombination ?: KeyCombination(KeyCode.UNDEFINED)
                val keybindButton = KeybindInputButton(initialKeybind) { newKeybind ->
                    mutableProperty?.setter?.call(field.objectInstance, newKeybind)
                    logger.debug("Keybind field '{}' changed to: {}", field.name, newKeybind)
                }

                val resetButton = Button("⟳").apply {
                    styleClass.add("keybind-reset-button")
                    setOnAction {
                        val defaultValue = SettingsManager.getDefaultValue(field.property) as? KeyCombination
                        defaultValue?.let { it ->
                            keybindButton.currentKeybind = it
                            mutableProperty?.setter?.call(field.objectInstance, defaultValue)
                            logger.debug("Keybind field '{}' reset to default: {}", field.name, defaultValue)
                        }
                    }
                }

                HBox(5.0).apply {
                    children.addAll(keybindButton, resetButton)
                }
            }
        }
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