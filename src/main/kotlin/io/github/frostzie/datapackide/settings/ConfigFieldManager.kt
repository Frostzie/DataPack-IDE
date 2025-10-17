package io.github.frostzie.datapackide.settings

import io.github.frostzie.datapackide.settings.annotations.*
import io.github.frostzie.datapackide.settings.data.*
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.beans.property.Property
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

internal object ConfigFieldManager {
    private val logger = LoggerProvider.getLogger("ConfigFieldManager")

    fun create(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption): ConfigField? {
        val propValue = property.get(instance)

        return when {
            property.findAnnotation<ConfigEditorBoolean>() != null -> createBooleanField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorText>() != null -> createTextField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorSlider>() != null -> createSliderField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorDropdown>() != null -> createDropdownField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorButton>() != null -> createButtonField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorKeybind>() != null -> createKeybindField(instance, property, option, propValue)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createBooleanField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): BooleanConfigField? {
        if (propValue is Property<*> && propValue.value is Boolean) {
            return BooleanConfigField(
                instance, property as KProperty1<Any, Property<Boolean>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<Boolean>.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createTextField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): TextConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return TextConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String>.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createSliderField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): SliderConfigField? {
        if (propValue is Property<*> && propValue.value is Number) {
            return SliderConfigField(
                instance, property as KProperty1<Any, Property<Number>>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<Number>.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createDropdownField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): DropdownConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return DropdownConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String>.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createButtonField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): ButtonConfigField? {
        if (propValue is Function0<*>) {
            return ButtonConfigField(
                instance, property as KProperty1<Any, () -> Unit>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected () -> Unit.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createKeybindField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): KeybindConfigField? {
        if (propValue is Property<*> && propValue.value is KeyCombination) {
            return KeybindConfigField(
                instance,
                property as KProperty1<Any, Property<KeyCombination>>,
                option.name,
                option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<KeyCombination>.")
        return null
    }
}