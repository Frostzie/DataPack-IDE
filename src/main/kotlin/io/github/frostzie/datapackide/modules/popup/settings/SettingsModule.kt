package io.github.frostzie.datapackide.modules.popup.settings

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.screen.elements.popup.settings.SettingsView
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.data.CategoryItem
import io.github.frostzie.datapackide.settings.data.CategoryType
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.data.SearchResult
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Dialog
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.beans.value.ChangeListener
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.stage.Stage
import io.github.frostzie.datapackide.settings.categories.ThemeConfig

class SettingsModule(private val parentStage: Stage) {
    companion object {
        private val logger = LoggerProvider.getLogger("SettingsModule")
        // Relevance scores for search
        private const val EXACT_NAME_SCORE = 100
        private const val NAME_CONTAINS_SCORE = 50
        private const val DESC_CONTAINS_SCORE = 25
        private const val CATEGORY_NAME_SCORE = 15
        private const val CATEGORY_DESC_SCORE = 10
    }

    private var currentQuery: String = ""

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
        val view = SettingsView()

        val dialog = Dialog<ButtonType>()
        dialog.title = "Settings"
        dialog.isResizable = true
        dialog.dialogPane.content = view
        dialog.dialogPane.style = "-fx-font-size: ${ThemeConfig.fontSize.get()}px;"

        dialog.initOwner(parentStage)
        dialog.dialogPane.minWidth = UIConstants.SETTINGS_MIN_WIDTH
        dialog.dialogPane.minHeight = UIConstants.SETTINGS_MIN_HEIGHT
        dialog.dialogPane.prefWidth = UIConstants.SETTINGS_WIDTH
        dialog.dialogPane.prefHeight = UIConstants.SETTINGS_HEIGHT

        val applyBtn = ButtonType("Apply", ButtonBar.ButtonData.APPLY)
        val closeBtn = ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE)
        dialog.dialogPane.buttonTypes.addAll(applyBtn, closeBtn)

        val applyButton = dialog.dialogPane.lookupButton(applyBtn) as Button
        applyButton.isDefaultButton = false
        applyButton.addEventFilter(ActionEvent.ACTION) { event ->
            saveSettings()
            event.consume() // Prevents the dialog from closing
        }

        val fontSizeListener = ChangeListener<Number> { _, _, _ ->
            dialog.dialogPane.style = "-fx-font-size: ${ThemeConfig.fontSize.get()}px;"
        }
        ThemeConfig.fontSize.addListener(fontSizeListener)

        dialog.showAndWait()

        ThemeConfig.fontSize.removeListener(fontSizeListener)
        saveSettings()
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