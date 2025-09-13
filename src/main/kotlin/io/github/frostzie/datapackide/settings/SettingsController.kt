package io.github.frostzie.datapackide.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.UIAction
import io.github.frostzie.datapackide.events.UIActionEvent
import io.github.frostzie.datapackide.screen.elements.popup.SettingsWindow
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class SettingsController(private val parentStage: Stage?) {

    companion object {
        private val logger = LoggerProvider.getLogger("SettingsController")
    }

    internal var xOffset = 0.0
    internal var yOffset = 0.0
    internal var stage: Stage? = null
    private var currentCategoryIndex = 0
    private var currentSubCategory: String? = null

    private lateinit var view: SettingsWindow
    private lateinit var searchHandler: SettingsSearchHandler

    fun show() {
        stage = Stage().apply {
            initStyle(StageStyle.UNDECORATED)
            initModality(Modality.APPLICATION_MODAL)
            parentStage?.let { initOwner(it) }
            isResizable = true
            minWidth = 800.0
            minHeight = 600.0
        }

        view = SettingsWindow(this)
        val content = view.createContent()

        searchHandler = SettingsSearchHandler(
            searchField = view.searchField,
            categoryTreeView = view.categoryTreeView,
            searchResults = view.searchResults,
            onNavigate = { result -> navigateToSetting(result) },
            onExitSearch = { updateContentArea() }
        ).apply { initialize() }

        // Initialize with first category
        Platform.runLater {
            view.categoryTreeView.selectionModel.select(1)
        }

        val scene = Scene(content, 900.0, 700.0).apply {
            fill = Color.TRANSPARENT
        }

        try {
            CSSManager.applyPopupStyles(scene, "Settings.css")
        } catch (e: Exception) {
            logger.warn("Could not load Settings CSS: ${e.message}")
        }

        stage?.scene = scene
        stage?.centerOnScreen()
        stage?.showAndWait()

        logger.debug("Settings window closed")
    }

    fun handleClose() {
        logger.debug("Settings window closing")
        stage?.close()
    }

    fun handleSave() {
        logger.debug("Apply settings requested")
        EventBus.post(UIActionEvent(UIAction.SAVE_SETTINGS))
    }

    fun handleCategorySelection(categoryItem: SettingsWindow.CategoryItem) {
        if (searchHandler.isSearching()) return

        when (categoryItem.type) {
            SettingsWindow.CategoryType.MAIN_CATEGORY -> {
                currentCategoryIndex = SettingsManager.getConfigCategories().indexOfFirst { it.second == categoryItem.configClass }
                currentSubCategory = null
                updateContentArea()
            }
            SettingsWindow.CategoryType.SUB_CATEGORY -> {
                currentCategoryIndex = SettingsManager.getConfigCategories().indexOfFirst { it.second == categoryItem.configClass }
                currentSubCategory = categoryItem.subCategory
                updateContentArea()
            }
            SettingsWindow.CategoryType.ROOT -> {
                // Do nothing for root selection
            }
        }
    }

    fun updateContentArea() {
        view.contentArea.children.clear()
        val category = SettingsManager.getConfigCategories()[currentCategoryIndex]

        if (currentSubCategory != null) {
            view.contentArea.children.add(view.createSubCategoryContent(category.second, currentSubCategory!!))
        } else {
            view.contentArea.children.add(view.createFullCategoryContent(category.first, category.second))
        }
    }

    fun navigateToSetting(result: SettingsManager.SearchResult) {
        val categories = SettingsManager.getConfigCategories()
        val categoryIndex = categories.indexOfFirst { it.first == result.mainCategory }

        if (categoryIndex >= 0) {
            currentCategoryIndex = categoryIndex
            currentSubCategory = result.subCategory
            view.selectTreeItem(categoryIndex, result.subCategory)
            updateContentArea()
        }
    }
}