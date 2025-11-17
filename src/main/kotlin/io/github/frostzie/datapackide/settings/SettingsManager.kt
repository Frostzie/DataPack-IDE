package io.github.frostzie.datapackide.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.settings.annotations.*
import io.github.frostzie.datapackide.settings.data.BooleanConfigField
import io.github.frostzie.datapackide.settings.data.ButtonConfigField
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.data.DropdownConfigField
import io.github.frostzie.datapackide.settings.data.InfoConfigField
import io.github.frostzie.datapackide.settings.data.KeybindConfigField
import io.github.frostzie.datapackide.settings.data.SliderConfigField
import io.github.frostzie.datapackide.settings.data.TextConfigField
import io.github.frostzie.datapackide.utils.LoggerProvider
import java.io.FileReader
import java.io.FileWriter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object SettingsManager {
    private val logger = LoggerProvider.getLogger("SettingsManager")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val settingsFile = ConfigManager.configDir.resolve("settings.json").toFile()
    private val defaultValues = mutableMapOf<KProperty1<*, *>, Any?>()

    private val configClasses = mutableListOf<Pair<String, KClass<*>>>()

    fun register(categoryName: String, configClass: KClass<*>) {
        configClasses.add(categoryName to configClass)
        logger.debug("Registered settings category '$categoryName' with ${configClass.simpleName}")
    }

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
                    is InfoConfigField -> { /* Info fields don't have a value to cache */ }
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
                    val p = property as KProperty1<Any, Any>
                    ConfigFieldManager.create(objectInstance, p, option)
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
                            is InfoConfigField -> { /* Skip info */ }
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
                                        is InfoConfigField -> { /* Skip info */ }
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