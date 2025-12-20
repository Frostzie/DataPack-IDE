package io.github.frostzie.datapackide.screen.elements.main

import atlantafx.base.controls.Tab
import atlantafx.base.controls.TabLine
import atlantafx.base.theme.Styles
import io.github.frostzie.datapackide.features.FeatureRegistry
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.fxmisc.flowless.VirtualizedScrollPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL

/**
 * View for the text editor that displays multiple tabs using AtlantaFX TabLine.
 * Each tab contains a CodeArea with the text editor.
 */
class TextEditorView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TextEditorView")
    }

    internal val viewModel = TextEditorViewModel()
    private val tabLine = TabLine()
    private val contentArea = StackPane()
    private val decoratorCleanups = mutableMapOf<String, MutableList<() -> Unit>>()

    init {
        styleClass.add("text-editor-container")

        setupTabLine()
        setupContentArea()
        setupListeners()

        children.addAll(tabLine, contentArea)
        setVgrow(contentArea, Priority.ALWAYS)
    }

    /**
     * Configures the TabLine with AtlantaFX styles and policies
     */
    //TODO: Remove animation
    private fun setupTabLine() {
        tabLine.styleClass.add(Styles.TABS_BORDER_TOP)
        tabLine.setTabDragPolicy(Tab.DragPolicy.REORDER)
        tabLine.setTabResizePolicy(Tab.ResizePolicy.COMPUTED_WIDTH)
        tabLine.setTabClosingPolicy(Tab.ClosingPolicy.SELECTED_TAB) //TODO: Possibly? add hover closing
    }

    private fun setupContentArea() {
        contentArea.styleClass.add("editor-content-area")
        setVgrow(contentArea, Priority.ALWAYS)
    }

    /**
     * Sets up listeners for ViewModel changes
     */
    private fun setupListeners() {
        // Listen for new tabs being added
        viewModel.tabs.addListener { change: ListChangeListener.Change<out TextEditorViewModel.TabData> ->
            while (change.next()) {
                if (change.wasAdded()) {
                    change.addedSubList.forEach { tabData ->
                        addTab(tabData)
                    }
                }
                if (change.wasRemoved()) {
                    change.removed.forEach { tabData ->
                        removeTab(tabData)
                    }
                }
            }
        }

        // Listen for active tab changes
        viewModel.activeTab.addListener { _, _, newTab ->
            if (newTab != null) {
                switchToTab(newTab)
            } else {
                contentArea.children.clear()
            }
        }

        // Listen for tab selection changes in the UI
        tabLine.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            if (newTab != null) {
                val tabData = viewModel.tabs.find { it.id == newTab.id }
                if (tabData != null && viewModel.activeTab.get() != tabData) {
                    viewModel.activeTab.set(tabData)
                }
            }
        }
    }

    /**
     * Adds a new tab to the TabLine for the given TabData
     */
    private fun addTab(tabData: TextEditorViewModel.TabData) {
        // Create a custom graphic for the tab content, allowing direct access to the label for styling
        val tabLabel = Label(tabData.displayName)
        val tabIcon = FontIcon(Material2AL.FOLDER)

        // An invisible placeholder that reserves space for the close button.
        // The width is an estimate of the close button's size. If you find a more exact one, change pls.
        val closeButtonPlaceholder = Region().apply {
            prefWidth = 22.0
        }

        val graphic = HBox(tabIcon, tabLabel, closeButtonPlaceholder).apply {
            alignment = Pos.CENTER_LEFT
            spacing = 5.0 // Space between icon and label
        }

        // We pass null for text and use our custom graphic instead
        val tab = Tab(tabData.id, null, graphic)
        tab.tooltip = Tooltip(tabData.filePath.toString())

        // When the tab is selected, the close button appears, so we hide the placeholder.
        // When it's deselected, we show the placeholder to keep the tab width consistent.
        val selectionListener = ChangeListener<Boolean> { _, _, isSelected ->
            closeButtonPlaceholder.isVisible = !isSelected
            closeButtonPlaceholder.isManaged = !isSelected
        }
        tab.selectedProperty().addListener(selectionListener)

        val cleanups = mutableListOf<() -> Unit>()
        FeatureRegistry.editorTabDecorators.forEach { decorator ->
            cleanups.add(decorator.decorate(tab, tabData))
        }
        decoratorCleanups[tabData.id] = cleanups

        tab.setOnCloseRequest { event ->
            tab.selectedProperty().removeListener(selectionListener) // Clean up listener
            viewModel.closeTab(tabData)
            event.consume()
        }

        tabLine.tabs.add(tab)
        tabLine.selectionModel.select(tab)

        logger.debug("Added tab: ${tabData.displayName}, ID: ${tabData.id}")
    }

    /**
     * Removes a tab from the TabLine
     */
    private fun removeTab(tabData: TextEditorViewModel.TabData) {
        tabLine.tabs.find { it.id == tabData.id }?.let {
            tabLine.tabs.remove(it)
        }

        // Execute and remove all cleanup functions associated with the closed tab
        decoratorCleanups.remove(tabData.id)?.forEach { cleanup ->
            try {
                cleanup()
            } catch (e: Exception) {
                logger.error("Error during tab decorator cleanup for ${tabData.displayName}", e)
            }
        }
        logger.debug("Removed tab and cleaned up decorators: ${tabData.displayName}, ID: ${tabData.id}")
    }

    /**
     * Switches the content area to display the CodeArea for the given tab
     */
    private fun switchToTab(tabData: TextEditorViewModel.TabData) {
        contentArea.children.clear()
        contentArea.children.add(VirtualizedScrollPane(tabData.codeArea))

        // Select the corresponding tab in the TabLine
        val tab = tabLine.tabs.find { it.id == tabData.id }
        if (tab != null && tabLine.selectionModel.selectedItem != tab) {
            tabLine.selectionModel.select(tab)
        }

        Platform.runLater {
            tabData.codeArea.requestFocus()
        }

        logger.debug("Switched to tab: ${tabData.displayName}, ID: ${tabData.id}")
    }

    /**
     * Request focus for the active tab's CodeArea
     */
    override fun requestFocus() {
        super.requestFocus()
        viewModel.activeTab.get()?.codeArea?.requestFocus()
    }

    /**
     * Cleanup method
     */
    fun cleanup() {
        viewModel.cleanup()
        logger.info("TextEditorView closed")
    }
}