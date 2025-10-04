package io.github.frostzie.datapackide.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.SettingsWindowCloseSave
import io.github.frostzie.datapackide.settings.annotations.*
import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import io.github.frostzie.datapackide.settings.categories.MainConfig
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
        registerEventHandlers()
        loadSettings()
        logger.info("SettingsManager initialization complete")
    }

    private fun cacheDefaultValues() {
        logger.debug("Caching default setting values...")
        configClasses.forEach { (_, configClass) ->
            val objectInstance = configClass.objectInstance
            if (objectInstance != null) {
                configClass.declaredMemberProperties.forEach { prop ->
                    if (prop.findAnnotation<Expose>() != null) {
                        @Suppress("UNCHECKED_CAST")
                        val propertyObject = (prop as KProperty1<Any, *>).get(objectInstance)
                        if (propertyObject is Property<*>) {
                            defaultValues[prop] = propertyObject.value
                        }
                    }
                }
            }
        }
        logger.debug("Cached ${defaultValues.size} default values.")
    }

    private fun registerEventHandlers() {
        EventBus.register(this)
    }
    @SubscribeEvent
    fun onSettingsSaveRequest(event: SettingsWindowCloseSave) {
        saveSettings()
    }

    fun getDefaultValue(property: KProperty1<*, *>): Any? = defaultValues[property]

    fun getConfigCategories(): List<Pair<String, KClass<*>>> = configClasses

    fun getConfigFields(configClass: KClass<*>): List<ConfigField> {
        val objectInstance = configClass.objectInstance ?: return emptyList()
        val propertiesByName = configClass.declaredMemberProperties.associateBy { it.name }

        return configClass.java.declaredFields.mapNotNull { field -> propertiesByName[field.name] }
            .mapNotNull { property ->
                val expose = property.findAnnotation<Expose>()
                val option = property.findAnnotation<ConfigOption>()

                if (expose != null && option != null) {
                    property.isAccessible = true

                    val editorType = when {
                        property.findAnnotation<ConfigEditorBoolean>() != null -> EditorType.BOOLEAN
                        property.findAnnotation<ConfigEditorText>() != null -> EditorType.TEXT
                        property.findAnnotation<ConfigEditorSlider>() != null -> EditorType.SLIDER
                        property.findAnnotation<ConfigEditorDropdown>() != null -> EditorType.DROPDOWN
                        property.findAnnotation<ConfigEditorButton>() != null -> EditorType.BUTTON
                        property.findAnnotation<ConfigEditorKeybind>() != null -> EditorType.KEYBIND
                        else -> EditorType.TEXT
                    }

                    ConfigField(
                        objectInstance = objectInstance,
                        property = property as KProperty1<Any, Any>,
                        name = option.name,
                        description = option.desc,
                        editorType = editorType,
                        category = property.findAnnotation<ConfigCategory>(),
                        sliderAnnotation = property.findAnnotation<ConfigEditorSlider>(),
                        dropdownAnnotation = property.findAnnotation<ConfigEditorDropdown>(),
                        buttonAnnotation = property.findAnnotation<ConfigEditorButton>()
                    )
                } else null
            }
    }

    fun getNestedCategories(configClass: KClass<*>): Map<String, List<ConfigField>> {
        val fields = getConfigFields(configClass)
        return fields.groupBy { field ->
            field.category?.name ?: "General"
        }
    }

    fun searchSettings(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        if (query.isBlank()) return results

        val lowerQuery = query.lowercase()

        configClasses.forEach { (categoryName, configClass) ->
            val fields = getConfigFields(configClass)

            fields.forEach { field ->
                val relevanceScore = calculateRelevance(field, lowerQuery)
                if (relevanceScore > 0) {
                    results.add(
                        SearchResult(
                            mainCategory = categoryName,
                            subCategory = field.category?.name ?: "General",
                            field = field,
                            relevanceScore = relevanceScore
                        )
                    )
                }
            }
        }

        return results.sortedByDescending { it.relevanceScore }
    }

    private fun calculateRelevance(field: ConfigField, query: String): Int {
        var score = 0

        // Exact name match gets highest score
        if (field.name.lowercase() == query) score += 100

        // Name contains query
        if (field.name.lowercase().contains(query)) score += 50

        // Description contains query
        if (field.description.lowercase().contains(query)) score += 25

        // Category name contains query
        if (field.category?.name?.lowercase()?.contains(query) == true) score += 15

        // Category description contains query
        if (field.category?.desc?.lowercase()?.contains(query) == true) score += 10

        return score
    }

    fun saveSettings() {
        try {
            val jsonObject = JsonObject()

            configClasses.forEach { (categoryName, configClass) ->
                val categoryObject = JsonObject()
                val objectInstance = configClass.objectInstance

                if (objectInstance != null) {
                    getConfigFields(configClass).forEach { field ->
                        val prop = field.property.get(field.objectInstance) as? Property<*>
                        val value = prop?.value
                        when (value) {
                            is Boolean -> categoryObject.addProperty(field.property.name, value)
                            is String -> categoryObject.addProperty(field.property.name, value)
                            is Double -> categoryObject.addProperty(field.property.name, value)
                            is Int -> categoryObject.addProperty(field.property.name, value)
                            is KeyCombination -> categoryObject.add(field.property.name, gson.toJsonTree(value))
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
                                    @Suppress("UNCHECKED_CAST")
                                    val prop = field.property.get(objectInstance) as? Property<Any>
                                    if (prop == null) {
                                        logger.warn("Could not get Property object for ${field.name}")
                                        return@forEach
                                    }

                                    val value = when (field.editorType) {
                                        EditorType.BOOLEAN -> jsonElement.asBoolean
                                        EditorType.TEXT, EditorType.DROPDOWN -> jsonElement.asString
                                        EditorType.SLIDER -> jsonElement.asDouble
                                        EditorType.BUTTON -> null
                                        EditorType.KEYBIND -> gson.fromJson(jsonElement, KeyCombination::class.java)
                                    }

                                    if (value != null) {
                                        prop.value = value
                                        logger.debug("Loaded setting: {} = {}", field.name, value)
                                    } else {
                                        logger.debug("Skipping setting for button: ${field.name}")
                                    }
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

    data class ConfigField(
        val objectInstance: Any,
        val property: KProperty1<Any, Any>,
        val name: String,
        val description: String,
        val editorType: EditorType,
        val category: ConfigCategory? = null,
        val sliderAnnotation: ConfigEditorSlider? = null,
        val dropdownAnnotation: ConfigEditorDropdown? = null,
        val buttonAnnotation: ConfigEditorButton? = null
    )

    data class SearchResult(
        val mainCategory: String,
        val subCategory: String,
        val field: ConfigField,
        val relevanceScore: Int
    )

    enum class EditorType {
        BOOLEAN, TEXT, SLIDER, DROPDOWN, BUTTON, KEYBIND
    }
}