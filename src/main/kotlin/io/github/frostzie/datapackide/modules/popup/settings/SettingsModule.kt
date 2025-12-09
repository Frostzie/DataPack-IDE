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

/**
 * Manages the logic for the settings window, including searching, category selection,
 * displaying settings content, and saving changes. It acts as an intermediary between
 * the settings UI elements and the [SettingsManager].
 *
 * @param parentStage The parent JavaFX Stage for the settings dialog.
 */
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

    /**
     * Initiates a search for settings based on the provided query string.
     * The search results are then posted as a [SettingsSearchResultsAvailable] event.
     *
     * @param query The search string.
     */
    fun search(query: String) {
        this.currentQuery = query
        if (query.isBlank()) {
            EventBus.post(SettingsSearchResultsAvailable(query, emptyList()))
        } else {
            val results = searchSettings(query)
            EventBus.post(SettingsSearchResultsAvailable(query, results))
        }
    }

    /**
     * Handles the selection of a settings category (either main or subcategory) from the navigation tree.
     * It retrieves the relevant settings fields and updates the content area of the settings window
     * by posting a [SettingsContentUpdate] event. The order of sections reflects the declaration order.
     *
     * @param item The [CategoryItem] representing the selected category.
     */
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

    /**
     * Handles the selection of a search result, navigating the settings tree to highlight
     * the corresponding category and field.
     *
     * @param result The [SearchResult] that was selected.
     */
    fun selectSearchResult(result: SearchResult) {
        val categories = SettingsManager.getConfigCategories()
        val categoryIndex = categories.indexOfFirst { it.first == result.mainCategory }

        if (categoryIndex != -1) {
            EventBus.post(SelectTreeItem(categoryIndex, result.subCategory))
        }
    }

    /**
     * Displays the main settings window dialog.
     */
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

    /**
     * Loads the available setting categories and their subcategories from the [SettingsManager]
     * and sends them to the UI components via a [SettingsCategoriesAvailable] event.
     * This method ensures the categories are sent in their declared order.
     */
    fun loadAndSendCategories() {
        val categoryDataList = SettingsManager.getConfigCategories().map { (categoryName, configClass) ->
            // Subcategories are retrieved in their declared order due to the LinkedHashMap in SettingsManager.
            val subCategories = SettingsManager.getNestedCategories(configClass).keys.toList()
            CategoryData(categoryName.replaceFirstChar { it.uppercase() }, configClass, subCategories)
        }
        EventBus.post(SettingsCategoriesAvailable(categoryDataList))
    }

    /**
     * Triggers the saving of all current settings values through the [SettingsManager].
     */
    fun saveSettings() {
        SettingsManager.saveSettings()
        logger.debug("Settings saved.")
    }

    /**
     * Performs a search across all registered settings and returns a list of matching [SearchResult]s.
     * Results are ranked by relevance.
     *
     * @param query The search query string.
     * @return A list of [SearchResult]s, sorted by relevance score in descending order.
     */
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

    /**
     * Calculates a relevance score for a given [ConfigField] against a search query.
     *
     * @param field The [ConfigField] to evaluate.
     * @param query The search query in lowercase.
     * @return An integer representing the relevance score. Higher scores mean more relevant.
     */
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