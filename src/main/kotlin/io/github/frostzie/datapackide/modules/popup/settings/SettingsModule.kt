package io.github.frostzie.datapackide.modules.popup.settings

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.handlers.popup.settings.SettingsHandler
import io.github.frostzie.datapackide.screen.elements.popup.settings.SettingsView
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.data.CategoryItem
import io.github.frostzie.datapackide.settings.data.CategoryType
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.data.SearchResult
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class SettingsModule(private val parentStage: Stage) {
    companion object {
        private val logger = LoggerProvider.getLogger("SettingsModule")
    }

    private var handler: SettingsHandler = SettingsHandler(this)
    var stage: Stage? = null
    var xOffset = 0.0
    var yOffset = 0.0

    init {
        EventBus.register(handler)
        logger.info("SettingsModule initialized and handler registered.")
    }

    fun dragWindow(screenX: Double, screenY: Double) {
        stage?.x = screenX - xOffset
        stage?.y = screenY - yOffset
    }

    fun search(query: String) {
        if (query.isBlank()) {
            EventBus.post(SettingsSearchResultsAvailable(emptyList()))
        } else {
            val results = searchSettings(query)
            EventBus.post(SettingsSearchResultsAvailable(results))
        }
    }

    fun selectCategory(item: CategoryItem) {
        val sections = mutableListOf<SectionData>()
        val title: String

        when (item.type) {
            CategoryType.MAIN_CATEGORY -> {
                title = "${item.name} Settings"
                item.configClass?.let { configClass ->
                    val nestedCategories = SettingsManager.getNestedCategories(configClass)
                    nestedCategories.forEach { (subCategoryName, fields) ->
                        sections.add(SectionData(subCategoryName, fields.firstOrNull()?.category?.desc, fields))
                    }
                }
            }
            CategoryType.SUB_CATEGORY -> {
                title = "${item.subCategory} Settings"
                item.configClass?.let { configClass ->
                    item.subCategory?.let { subCategory ->
                        val nestedCategories = SettingsManager.getNestedCategories(configClass)
                        val fields = nestedCategories[subCategory] ?: emptyList()
                        sections.add(SectionData(subCategory, fields.firstOrNull()?.category?.desc, fields))
                    }
                }
            }
            else -> return
        }

        EventBus.post(SettingsContentUpdate(title, sections))
    }

    fun selectSearchResult(result: SearchResult) {
        val categories = SettingsManager.getConfigCategories()
        val categoryIndex = categories.indexOfFirst { it.first == result.mainCategory }

        if (categoryIndex != -1) {
            EventBus.post(SelectTreeItem(categoryIndex, result.subCategory))
            EventBus.post(SettingsSearchResultsAvailable(emptyList()))
        }
    }

    fun showSettingsWindow() {
        if (stage == null) {
            stage = Stage().apply {
                initStyle(StageStyle.UNDECORATED)
                initModality(Modality.APPLICATION_MODAL)
                initOwner(parentStage)
                isResizable = true
                minWidth = 800.0
                minHeight = 600.0
            }

            val view = SettingsView()
            val scene = Scene(view, 900.0, 700.0).apply { fill = Color.TRANSPARENT }
            CSSManager.applyPopupStyles(scene, "Settings.css")
            stage?.scene = scene
            stage?.centerOnScreen()
        }
        stage?.showAndWait()
    }

    fun loadAndSendCategories() {
        val categoryDataList = SettingsManager.getConfigCategories().map { (categoryName, configClass) ->
            val subCategories = SettingsManager.getNestedCategories(configClass).keys.sorted()
            CategoryData(categoryName.replaceFirstChar { it.uppercase() }, configClass, subCategories)
        }
        EventBus.post(SettingsCategoriesAvailable(categoryDataList))
    }

    fun saveSettings() {
        SettingsManager.saveSettings()
        logger.debug("Settings saved.")
    }

    fun closeSettings() {
        stage?.close()
    }

    private fun searchSettings(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        if (query.isBlank()) return results

        val lowerQuery = query.lowercase()

        SettingsManager.getConfigCategories().forEach { (categoryName, configClass) ->
            val fields = SettingsManager.getConfigFields(configClass)

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

        // Exact name match gets the highest score
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
}