package io.github.frostzie.datapackide.modules.popup.settings

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.screen.elements.popup.settings.SettingsView
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.data.CategoryItem
import io.github.frostzie.datapackide.settings.data.CategoryType
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.data.SearchResult
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class SettingsModule(private val parentStage: Stage, private val themeModule: ThemeModule) {
    companion object {
        private val logger = LoggerProvider.getLogger("SettingsModule")
        // Relevance scores for search
        private const val EXACT_NAME_SCORE = 100
        private const val NAME_CONTAINS_SCORE = 50
        private const val DESC_CONTAINS_SCORE = 25
        private const val CATEGORY_NAME_SCORE = 15
        private const val CATEGORY_DESC_SCORE = 10
    }

    var stage: Stage? = null
    var xOffset = 0.0
    var yOffset = 0.0
    private var currentQuery: String = ""

    fun dragWindow(screenX: Double, screenY: Double) {
        stage?.x = screenX - xOffset
        stage?.y = screenY - yOffset
    }

    fun search(query: String) {
        this.currentQuery = query
        if (query.isBlank()) {
            EventBus.post(SettingsSearchResultsAvailable(query, emptyList()))
        } else {
            val results = searchSettings(query)
            EventBus.post(SettingsSearchResultsAvailable(query, results))
        }
    }

    fun selectCategory(item: CategoryItem) {
        val sections = mutableListOf<SectionData>()
        val title: String
        var filterFields: Set<ConfigField>? = null

        if (currentQuery.isNotBlank()) {
            filterFields = searchSettings(currentQuery).map { it.field }.toSet()
        }

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

        EventBus.post(SettingsContentUpdate(title, sections, filterFields))
    }

    fun selectSearchResult(result: SearchResult) {
        val categories = SettingsManager.getConfigCategories()
        val categoryIndex = categories.indexOfFirst { it.first == result.mainCategory }

        if (categoryIndex != -1) {
            EventBus.post(SelectTreeItem(categoryIndex, result.subCategory))
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
            themeModule.scenes.add(scene)
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
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase()

        return SettingsManager.getConfigCategories()
            .flatMap { (categoryName, configClass) ->
                SettingsManager.getConfigFields(configClass).mapNotNull { field ->
                val relevanceScore = calculateRelevance(field, lowerQuery)
                if (relevanceScore > 0) {
                        SearchResult(categoryName, field.category?.name ?: "General", field, relevanceScore)
                } else null
            }
        }
        .sortedByDescending { it.relevanceScore }
    }

    private fun calculateRelevance(field: ConfigField, query: String): Int {
        var score = 0

        // Exact name match gets the highest score
        if (field.name.lowercase() == query) score += EXACT_NAME_SCORE

        // Name contains query
        if (field.name.lowercase().contains(query)) score += NAME_CONTAINS_SCORE

        // Description contains query
        if (field.description.lowercase().contains(query)) score += DESC_CONTAINS_SCORE

        // Category name contains query
        if (field.category?.name?.lowercase()?.contains(query) == true) score += CATEGORY_NAME_SCORE

        // Category description contains query
        if (field.category?.desc?.lowercase()?.contains(query) == true) score += CATEGORY_DESC_SCORE

        return score
    }
}