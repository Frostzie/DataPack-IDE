package io.github.frostzie.datapackide.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.settings.annotations.*
import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import io.github.frostzie.datapackide.settings.categories.MainConfig
import io.github.frostzie.datapackide.settings.data.BooleanConfigField
import io.github.frostzie.datapackide.settings.data.ButtonConfigField
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.data.DropdownConfigField
import io.github.frostzie.datapackide.settings.data.KeybindConfigField
import io.github.frostzie.datapackide.settings.data.SliderConfigField
import io.github.frostzie.datapackide.settings.data.TextConfigField
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.beans.property.Property
import java.io.FileReader
import java.io.FileWriter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

object SettingsManager {
    private val logger = LoggerProvider.getLogger("SettingsManager")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val settingsFile = ConfigManager.configDir.resolve("settings.json").toFile()
    private val defaultValues = mutableMapOf<KProperty1<*, *>, Any?>()

    private val configClasses = listOf(
        "main" to MainConfig::class, //TODO: Remove Example settings
        //"theme" to ThemeConfig::class, TODO: Add theme settings
        //"keybinds" to KeybindConfig::class, TODO: Add keybind settings
        "advanced" to AdvancedConfig::class
    )

    fun initialize() {
        logger.info("Initializing SettingsManager...")
        cacheDefaultValues()
        loadSettings()
        logger.info("SettingsManager initialization complete")
    }

    private fun cacheDefaultValues() {
        logger.debug("Caching default setting values...")
        configClasses.forEach { (_, configClass) ->
            getConfigFields(configClass).forEach { field ->
                when (field) {
                    is ButtonConfigField -> { /* Buttons don't have a value to cache */ }
                    is BooleanConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is DropdownConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is KeybindConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is SliderConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is TextConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                }
            }
        }
        logger.debug("Cached ${defaultValues.size} default values.")
    }

    fun getDefaultValue(property: KProperty1<*, *>): Any? = defaultValues[property]

    fun getConfigCategories(): List<Pair<String, KClass<*>>> = configClasses

    @Suppress("UNCHECKED_CAST") // Only to not show warning in IntelliJ
    fun getConfigFields(configClass: KClass<*>): List<ConfigField> {
        val objectInstance = configClass.objectInstance ?: return emptyList()
        val propertiesByName = configClass.declaredMemberProperties.associateBy { it.name }

        return configClass.java.declaredFields.mapNotNull { field -> propertiesByName[field.name] }
            .mapNotNull { property ->
                val expose = property.findAnnotation<Expose>()
                val option = property.findAnnotation<ConfigOption>()

                if (expose != null && option != null) {
                    property.isAccessible = true
                    val p = property as KProperty1<Any, Any>
                    val propValue = p.get(objectInstance)

                    when {
                        property.findAnnotation<ConfigEditorBoolean>() != null -> {
                            if (propValue is Property<*> && propValue.value is Boolean) {
                                BooleanConfigField(
                                    objectInstance, p as KProperty1<Any, Property<Boolean>>, option.name, option.desc,
                                    property.findAnnotation()
                                )
                            } else {
                                logger.warn("Mismatched annotation/type for ${property.name} in ${configClass.simpleName}. Expected Property<Boolean>.")
                                null
                            }
                        }
                        property.findAnnotation<ConfigEditorText>() != null -> {
                            if (propValue is Property<*> && propValue.value is String) {
                                TextConfigField(
                                    objectInstance, p as KProperty1<Any, Property<String>>, option.name, option.desc,
                                    property.findAnnotation()
                                )
                            } else {
                                logger.warn("Mismatched annotation/type for ${property.name} in ${configClass.simpleName}. Expected Property<String>.")
                                null
                            }
                        }
                        property.findAnnotation<ConfigEditorSlider>() != null -> {
                            if (propValue is Property<*> && propValue.value is Number) {
                                SliderConfigField(
                                    objectInstance, p as KProperty1<Any, Property<Number>>, option.name, option.desc,
                                    property.findAnnotation(), property.findAnnotation()!!
                                )
                            } else {
                                logger.warn("Mismatched annotation/type for ${property.name} in ${configClass.simpleName}. Expected Property<Number>.")
                                null
                            }
                        }
                        property.findAnnotation<ConfigEditorDropdown>() != null -> {
                            if (propValue is Property<*> && propValue.value is String) {
                                DropdownConfigField(
                                    objectInstance, p as KProperty1<Any, Property<String>>, option.name, option.desc,
                                    property.findAnnotation(), property.findAnnotation()!!
                                )
                            } else {
                                                                logger.warn("Mismatched annotation/type for ${property.name} in ${configClass.simpleName}. Expected Property<String>.")
                                null
                            }
                        }
                        property.findAnnotation<ConfigEditorButton>() != null -> {
                            if (propValue is Function0<*>) {
                                ButtonConfigField(
                                    objectInstance, p as KProperty1<Any, () -> Unit>, option.name, option.desc,
                                    property.findAnnotation(), property.findAnnotation()!!
                                )
                            } else {
                                logger.warn("Mismatched annotation/type for ${property.name} in ${configClass.simpleName}. Expected () -> Unit.")
                                null
                            }
                        }
                        property.findAnnotation<ConfigEditorKeybind>() != null -> {
                            if (propValue is Property<*> && propValue.value is KeyCombination) {
                                KeybindConfigField(
                                    objectInstance,
                                    p as KProperty1<Any, Property<KeyCombination>>,
                                    option.name,
                                    option.desc,
                                    property.findAnnotation()
                                )
                            } else {
                                logger.warn("Mismatched annotation/type for ${property.name} in ${configClass.simpleName}. Expected Property<KeyCombination>.")
                                null
                            }
                        }
                        else -> null
                    }
                } else null
            }
    }

    fun getNestedCategories(configClass: KClass<*>): Map<String, List<ConfigField>> {
        val fields = getConfigFields(configClass)
        return fields.groupBy { field ->
            field.category?.name ?: "General"
        }
    }

    fun saveSettings() {
        try {
            val jsonObject = JsonObject()

            configClasses.forEach { (categoryName, configClass) ->
                val categoryObject = JsonObject()
                val objectInstance = configClass.objectInstance

                if (objectInstance != null) {
                    getConfigFields(configClass).forEach { field ->
                        when (field) {
                            is ButtonConfigField -> { /* Skip buttons */ }
                            is BooleanConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is DropdownConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is KeybindConfigField -> categoryObject.add(field.property.name, gson.toJsonTree(field.property.get(objectInstance).value))
                            is SliderConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is TextConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                        }
                    }
                }

                jsonObject.add(categoryName, categoryObject)
            }

            FileWriter(settingsFile).use { writer ->
                gson.toJson(jsonObject, writer)
            }

                        logger.info("Settings saved to ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to save settings", e)
        }
    }

    fun loadSettings() {
        if (!settingsFile.exists()) {
                        logger.info("Settings file doesn't exist, creating with defaults")
            return
        }

        try {
            FileReader(settingsFile).use { reader ->
                val jsonObject = gson.fromJson(reader, JsonObject::class.java)

                configClasses.forEach { (categoryName, configClass) ->
                    val categoryObject = jsonObject.getAsJsonObject(categoryName)
                    val objectInstance = configClass.objectInstance

                    if (categoryObject != null && objectInstance != null) {
                        getConfigFields(configClass).forEach { field ->
                            val jsonElement = categoryObject.get(field.property.name)
                            if (jsonElement != null && !jsonElement.isJsonNull) {
                                try {
                                    when (field) {
                                        is ButtonConfigField -> { /* Skip buttons */ }
                                        is BooleanConfigField -> field.property.get(objectInstance).value = jsonElement.asBoolean
                                        is DropdownConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                        is KeybindConfigField -> field.property.get(objectInstance).value = gson.fromJson(jsonElement, KeyCombination::class.java)
                                        is SliderConfigField -> field.property.get(objectInstance).value = jsonElement.asDouble
                                        is TextConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                    }
                                    logger.debug("Loaded setting: {} = {}", field.name, jsonElement)
                                } catch (e: Exception) {
                                    logger.warn("Failed to load setting ${field.name}: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }

                        logger.info("Settings loaded from ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to load settings", e)
        }
    }
}