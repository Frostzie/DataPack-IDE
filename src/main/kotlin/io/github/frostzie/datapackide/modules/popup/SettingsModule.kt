package io.github.frostzie.datapackide.modules.popup

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.eventsOLD.PopulateSettingsContentEvent
import io.github.frostzie.datapackide.eventsOLD.SelectTreeItemEvent
import io.github.frostzie.datapackide.eventsOLD.ShowSearchResultsEvent
import io.github.frostzie.datapackide.eventsOLD.UIAction
import io.github.frostzie.datapackide.eventsOLD.UIActionEvent
import io.github.frostzie.datapackide.handlers.popup.SettingsHandler
import io.github.frostzie.datapackide.screen.elements.popup.SettingsView
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.Modality
import javafx.stage.StageStyle

class SettingsModule(private val parentStage: Stage) {
    companion object {
        private val logger = LoggerProvider.getLogger("SettingsModule")
    }

    private lateinit var view: SettingsView
    private lateinit var handler: SettingsHandler
    var stage: Stage? = null
    var xOffset = 0.0
    var yOffset = 0.0

    init {
        EventBus.register(this)
        logger.info("SettingsModule initialized and listening for UIActionEvent.SHOW_SETTINGS")
    }

    @SubscribeEvent //TODO: Change
    fun onShowSettingsRequest(event: UIActionEvent) {
        if (event.action == UIAction.SHOW_SETTINGS) {
            showSettingsWindow()
        }
    }

    private fun showSettingsWindow() {
        if (!::view.isInitialized) {
            view = SettingsView(this)
            handler = SettingsHandler(this)
            EventBus.register(handler)
        }

        if (stage == null) {
            stage = Stage().apply {
                initStyle(StageStyle.UNDECORATED)
                initModality(Modality.APPLICATION_MODAL)
                initOwner(parentStage)
                isResizable = true
                minWidth = 800.0
                minHeight = 600.0
            }
            val content = view.createContent()
            val scene = Scene(content, 900.0, 700.0).apply { fill = Color.TRANSPARENT }
            CSSManager.applyPopupStyles(scene, "Settings.css")
            stage?.scene = scene
            stage?.centerOnScreen()
            Platform.runLater { view.categoryTreeView.selectionModel.select(1) } // Initialize with first category
        }
        stage?.showAndWait()
    }

    fun saveSettings() {
        SettingsManager.saveSettings()
        logger.info("Settings saved.")
    }

    fun closeSettings() {
        stage?.close()
        logger.info("Settings window closed.")
    }

    fun handleCategorySelection(item: SettingsView.CategoryItem) {
        val content = when (item.type) {
            SettingsView.CategoryType.MAIN_CATEGORY -> {
                item.configClass?.let { view.createFullCategoryContent(item.name, it) }
            }
            SettingsView.CategoryType.SUB_CATEGORY -> {
                item.configClass?.let { view.createSubCategoryContent(it, item.subCategory!!) }
            }
            else -> null
        }
        content?.let { EventBus.post(PopulateSettingsContentEvent(it)) }
    }

    fun handleSearch(query: String) {
        if (query.isBlank()) {
            EventBus.post(ShowSearchResultsEvent(emptyList()))
        } else {
            val results = SettingsManager.searchSettings(query)
            EventBus.post(ShowSearchResultsEvent(results))
        }
    }

    fun handleSearchResultSelection(result: SettingsManager.SearchResult) {
        val categories = SettingsManager.getConfigCategories()
        val categoryIndex = categories.indexOfFirst { it.first == result.mainCategory }

        if (categoryIndex != -1) {
            // Post event to select the tree item and switch view back to tree
            EventBus.post(SelectTreeItemEvent(categoryIndex, result.subCategory))
            EventBus.post(ShowSearchResultsEvent(emptyList())) // Hide search results
        }
    }
}