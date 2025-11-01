package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.OpenFile
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.web.WebView
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.readText

/**
 * ViewModel for the text editor that manages multiple tabs.
 * Each tab represents an open file with its own WebView instance.
 */
class TextEditorViewModel {
    private val logger = LoggerProvider.getLogger("TextEditorViewModel")

    /**
     * Data class representing a single editor tab
     */
    data class TabData(
        val id: String = UUID.randomUUID().toString(),
        val filePath: Path,
        val displayName: String,
        val webView: WebView,
        val bridge: EditorBridge
    )

    // Observable list of all open tabs
    val tabs: ObservableList<TabData> = FXCollections.observableArrayList()

    // Currently active tab
    val activeTab = SimpleObjectProperty<TabData?>()

    init {
        EventBus.register(this)
        logger.info("TextEditorViewModel initialized and registered with EventBus.")
    }

    /**
     * Opens a file in the editor. If the file is already open, switch to that tab.
     * Otherwise, create a new tab.
     */
    @SubscribeEvent
    fun onOpenFile(event: OpenFile) {
        Platform.runLater {
            val existingTab = tabs.find { it.filePath == event.path }
            if (existingTab != null) {
                logger.info("File already open, switching to tab: ${event.path.fileName}")
                activeTab.set(existingTab)
                return@runLater
            }

            logger.debug("Opening new file in tab: {}", event.path.fileName)
            createNewTab(event.path)
        }
    }

    /**
     * Creates a new tab for the given file path
     */
    private fun createNewTab(filePath: Path) {
        try {
            // Read file content
            val content = filePath.readText()
            logger.debug("Read file content: {} ({} characters)", filePath.fileName, content.length)

            // Create WebView and load editor HTML
            val webView = createWebView()

            // Create bridge with initial content
            val bridge = EditorBridge(
                webView = webView,
                filePath = filePath,
                initialContent = content,
                onContentSaved = {
                    logger.debug("Content saved callback for: {}", filePath.fileName)
                }
            )

            // Initialize bridge - this will load content when WebView is ready
            bridge.initialize()

            val tabData = TabData(
                filePath = filePath,
                displayName = filePath.fileName.toString(),
                webView = webView,
                bridge = bridge
            )

            tabs.add(tabData)
            activeTab.set(tabData)

            logger.debug("Tab created for file: {}", filePath.fileName)

        } catch (e: Exception) {
            logger.error("Failed to create tab for file: ${filePath.fileName}", e)
        }
    }

    /**
     * Creates a new WebView instance and loads the editor HTML
     */
    private fun createWebView(): WebView {
        val webView = WebView()

        // Load editor HTML from resources
        val editorUrl = javaClass.getResource("/assets/datapack-ide/editor/index.html")
        if (editorUrl != null) {
            webView.engine.load(editorUrl.toExternalForm())
            logger.debug("Loading editor from resources: {}", editorUrl)
        } else {
            logger.error("Editor HTML not found in resources!")
            webView.engine.loadContent("<html><body><h3>Error: Editor not found, report on github</h3></body></html>")
        }

        return webView
    }

    /**
     * Closes the specified tab and auto-saves before closing
     */
    fun closeTab(tabData: TabData) {
        logger.info("Closing tab: ${tabData.displayName}")

        // Auto-save before closing
        try {
            tabData.bridge.saveFile()
            logger.debug("Auto-saved file before closing: ${tabData.displayName}")
        } catch (e: Exception) {
            logger.error("Failed to auto-save before closing: ${tabData.displayName}", e)
        }

        tabs.remove(tabData)

        // If the closed tab was active, switch to another tab
        if (activeTab.get() == tabData) {
            activeTab.set(tabs.firstOrNull())
        }
    }

    /**
     * Saves the active tab's content
     */
    fun saveActiveTab() {
        val tab = activeTab.get() ?: return
        tab.bridge.saveFile()
        logger.info("Manually saved file: ${tab.displayName}")
    }

    /**
     * Cleanup method to be called when the editor is closed
     */
    fun cleanup() {
        // Auto-save all tabs before cleanup
        tabs.forEach { tab ->
            try {
                tab.bridge.saveFile()
                logger.debug("Auto-saved during cleanup: ${tab.displayName}")
            } catch (e: Exception) {
                logger.error("Failed to auto-save during cleanup: ${tab.displayName}", e)
            }
        }

        tabs.clear()
        activeTab.set(null)
        EventBus.unregister(this)
        logger.info("TextEditorViewModel cleaned up")
    }
}