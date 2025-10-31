package io.github.frostzie.datapackide.screen.elements.main

import atlantafx.base.controls.Tab
import atlantafx.base.controls.TabLine
import atlantafx.base.theme.Styles
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.collections.ListChangeListener
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL

/**
 * View for the text editor that displays multiple tabs using AtlantaFX TabLine.
 * Each tab contains a WebView with the code editor.
 */
class TextEditorView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TextEditorView")
    }

    internal val viewModel = TextEditorViewModel()
    private val tabLine = TabLine()
    private val contentArea = StackPane()

    init {
        styleClass.add("text-editor-container")

        setupTabLine()
        setupContentArea()
        setupListeners()

        children.addAll(tabLine, contentArea)
        setVgrow(contentArea, Priority.ALWAYS)

        logger.info("TextEditorView initialized")
    }

    /**
     * Configures the TabLine with AtlantaFX styles and policies
     */
    private fun setupTabLine() {
        tabLine.styleClass.add(Styles.TABS_BORDER_TOP)
        tabLine.setTabDragPolicy(Tab.DragPolicy.REORDER)
        tabLine.setTabResizePolicy(Tab.ResizePolicy.COMPUTED_WIDTH) // Change
        tabLine.setTabClosingPolicy(Tab.ClosingPolicy.SELECTED_TAB)
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
        // AtlantaFX Tab constructor: Tab(id, text, graphic)
        val tab = Tab(tabData.id, tabData.displayName, FontIcon(Material2AL.FOLDER))
        tab.setTooltip(Tooltip(tabData.filePath.toString()))

        tab.setOnCloseRequest {
            viewModel.closeTab(tabData)
        }

        tabLine.tabs.add(tab)

        tabLine.selectionModel.select(tab)

        logger.debug("Added tab: ${tabData.displayName}, ID: ${tabData.id}")
    }

    /**
     * Removes a tab from the TabLine
     */
    private fun removeTab(tabData: TextEditorViewModel.TabData) {
        val tab = tabLine.tabs.find { it.id == tabData.id }
        if (tab != null) {
            tabLine.tabs.remove(tab)
            logger.debug("Removed tab: ${tabData.displayName}, ID: ${tabData.id}")
        }
    }

    /**
     * Switches the content area to display the WebView for the given tab
     */
    private fun switchToTab(tabData: TextEditorViewModel.TabData) {
        contentArea.children.clear()
        contentArea.children.add(tabData.webView)

        // Select the corresponding tab in the TabLine
        val tab = tabLine.tabs.find { it.id == tabData.id }
        if (tab != null && tabLine.selectionModel.selectedItem != tab) {
            tabLine.selectionModel.select(tab)
        }

        logger.debug("Switched to tab: ${tabData.displayName}, ID: ${tabData.id}")
    }

    /**
     * Request focus for the active tab's WebView
     */
    override fun requestFocus() {
        super.requestFocus()
        viewModel.activeTab.get()?.webView?.requestFocus()
        //TODO: JS Bridge - Focus the editor in the WebView
    }

    /**
     * Cleanup method
     */
    fun cleanup() {
        viewModel.cleanup()
        logger.info("TextEditorView closed")
    }
}