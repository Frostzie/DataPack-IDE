package io.github.frostzie.datapackide.settings

import io.github.frostzie.datapackide.screen.elements.popup.SettingsWindow
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.control.TreeView

/**
 * Manages the search functionality.
 */
class SettingsSearchHandler(
    private val searchField: TextField,
    private val categoryTreeView: TreeView<SettingsWindow.CategoryItem>,
    private val searchResults: ListView<SettingsManager.SearchResult>,
    private val onNavigate: (SettingsManager.SearchResult) -> Unit,
    private val onExitSearch: () -> Unit
) {
    private var isSearchMode = false
    private val logger = LoggerProvider.getLogger("SettingsSearchHandler")

    fun initialize() {
        searchField.textProperty().addListener { _, _, newValue ->
            performSearch(newValue)
        }

        searchResults.setOnMouseClicked {
            searchResults.selectionModel.selectedItem?.let { result ->
                onNavigate(result)
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            exitSearchMode()
            return
        }

        enterSearchMode()

        val results = SettingsManager.searchSettings(query)
        searchResults.items = FXCollections.observableArrayList(results)
        logger.debug("Search for '$query' returned ${results.size} results")
    }

    private fun enterSearchMode() {
        if (isSearchMode) return

        isSearchMode = true
        categoryTreeView.isVisible = false
        categoryTreeView.isManaged = false
        searchResults.isVisible = true
        searchResults.isManaged = true
    }

    fun exitSearchMode() {
        if (!isSearchMode) return

        isSearchMode = false
        categoryTreeView.isVisible = true
        categoryTreeView.isManaged = true
        searchResults.isVisible = false
        searchResults.isManaged = false

        onExitSearch()
    }

    fun isSearching(): Boolean = isSearchMode
}