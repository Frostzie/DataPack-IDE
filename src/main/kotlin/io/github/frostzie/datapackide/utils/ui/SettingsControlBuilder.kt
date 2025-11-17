package io.github.frostzie.datapackide.utils.ui

import io.github.frostzie.datapackide.settings.annotations.ConfigEditorSlider
import io.github.frostzie.datapackide.settings.data.*
import io.github.frostzie.datapackide.utils.LoggerProvider
import atlantafx.base.controls.ProgressSliderSkin
import atlantafx.base.controls.ToggleSwitch
import io.github.frostzie.datapackide.utils.ui.controls.KeybindInputButton
import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.util.converter.IntegerStringConverter
import kotlin.math.roundToInt

/**
 * Builder for creating JavaFX controls for different setting types.
 */
object SettingsControlBuilder {
    private val logger = LoggerProvider.getLogger("SettingsControlBuilder")

    fun createSliderValueLabel(field: SliderConfigField): Label {
        val prop = field.property.get(field.objectInstance)
        val sliderAnnotation = field.sliderAnnotation
        return Label().apply {
            styleClass.add("slider-value")
            text = formatSliderLabel(prop.value.toDouble(), sliderAnnotation)
            prop.addListener { _, _, newValue ->
                text = formatSliderLabel(newValue.toDouble(), sliderAnnotation)
            }
        }
    }

    private fun formatSliderLabel(value: Double, annotation: ConfigEditorSlider): String {
        val hasDecimals = annotation.stepSize < 1.0
        return if (hasDecimals) {
            "Value: %.2f".format(value)
        }
        else {
            "Value: %d".format(value.toInt()) 
        }
    }

    /**
     * Creates a setting tile UI component based on the provided [ConfigField].
     *
     * This function inspects the type of the [ConfigField] and constructs an appropriate
     * JavaFX control (e.g., a toggle switch for a boolean, a slider for a number)
     * wrapped in a standardized tile layout.
     *
     * @param field The configuration field for which to create a UI tile.
     * @return An [HBox] containing the complete UI for the setting.
     */
    fun createSettingTile(field: ConfigField): HBox {
        return when (field) {
            is BooleanConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val toggleSwitch = ToggleSwitch().apply {
                    selectedProperty().bindBidirectional(prop)
                }
                Tiles.DefaultTile(field.name, field.description.takeIf { it.isNotEmpty() }, toggleSwitch)
            }

            is KeybindConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val keybindInputButton = KeybindInputButton().apply {
                    keybindProperty.bindBidirectional(prop)
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, keybindInputButton)
            }

            is DropdownConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val comboBox = ComboBox<String>().apply {
                    items.addAll(*field.dropdownAnnotation.values)
                    valueProperty().bindBidirectional(prop)
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, comboBox)
            }

            is TextConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val textArea = TextArea().apply {
                    textProperty().bindBidirectional(prop)
                    isWrapText = true
                }
                Tiles.LowTile(field.name, field.description.takeIf { it.isNotEmpty() }, textArea)
            }

            is SliderConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val sliderAnnotation = field.sliderAnnotation
                val slider = Slider().apply {
                    min = sliderAnnotation.minValue
                    max = sliderAnnotation.maxValue
                    blockIncrement = sliderAnnotation.stepSize
                    valueProperty().bindBidirectional(prop)
                    prefWidth = 400.0
                }
                val valueLabel = createSliderValueLabel(field)

                val sliderControl = HBox(slider, valueLabel).apply {
                    spacing = 5.0
                    alignment = Pos.CENTER_LEFT
                }
                Tiles.LowTile(field.name, field.description.takeIf { it.isNotEmpty() }, sliderControl)
            }

            is ButtonConfigField -> {
                val action = field.property.get(field.objectInstance)
                val button = Button(field.buttonAnnotation.text).apply {
                    styleClass.add("field-button")
                    setOnAction {
                        try {
                            action.invoke()
                            logger.debug("Action button '{}' executed.", field.name)
                        } catch (e: Exception) {
                            logger.error("Error executing action for button '{}'", field.name, e)
                        }
                    }
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, button)
            }

            is InfoConfigField -> {
                Tiles.InfoTile(field.name, field.description.takeIf { it.isNotEmpty() })
            }

            is SpinnerConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val spinnerAnnotation = field.spinnerAnnotation
                val spinner = Spinner<Int>(spinnerAnnotation.minValue, spinnerAnnotation.maxValue, prop.value).apply {
                    isEditable = true
                    prefWidth = 120.0
                }

                spinner.valueProperty().addListener { _, _, newValue ->
                    if (prop.value != newValue) {
                        prop.value = newValue
                    }
                }
                prop.addListener { _, _, newValue ->
                    if (spinner.value != newValue) {
                        spinner.valueFactory.value = newValue
                    }
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, spinner)
            }
        }
    }
}