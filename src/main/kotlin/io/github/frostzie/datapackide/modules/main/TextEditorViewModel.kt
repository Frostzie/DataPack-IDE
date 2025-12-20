package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.OpenFile
import io.github.frostzie.datapackide.events.SaveAllFiles
import io.github.frostzie.datapackide.modules.bars.BottomBarModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableSet
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * ViewModel for the text editor that manages multiple tabs.
 * Each tab represents an open file with its own CodeArea instance.
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
        val codeArea: CodeArea,
        val isDirty: BooleanProperty = SimpleBooleanProperty(false),
        // Listeners to be managed for cleanup
        var isDirtyListener: ChangeListener<Boolean>? = null,
        var textListener: InvalidationListener? = null,
        var caretListener: InvalidationListener? = null
    )

    // Observable list of all open tabs
    val tabs: ObservableList<TabData> = FXCollections.observableArrayList()
    val dirtyFiles: ObservableSet<Path> = FXCollections.observableSet()

    // Currently active tab
    val activeTab = SimpleObjectProperty<TabData?>()

    // Current line and column of the caret in the active editor
    val currentLine: IntegerProperty = SimpleIntegerProperty(1)
    val currentColumn: IntegerProperty = SimpleIntegerProperty(1)

    init {
        EventBus.register(this)
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
     * Saves all modified files
     */
    @SubscribeEvent
    fun onSaveAll(event: SaveAllFiles) {
        logger.debug("Saving all modified files...")
        tabs.filter { it.isDirty.get() }.forEach { saveFile(it) }
    }

    /**
     * Creates a new tab for the given file path
     */
    private fun createNewTab(filePath: Path) {
        try {
            // Read file content
            val content = filePath.readText()
            logger.debug("Read file content: {} ({} characters)", filePath.fileName, content.length)

            // Create CodeArea
            val codeArea = CodeArea(content)
            codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
            codeArea.styleClass.add("code-area")

            val tabData = TabData(
                filePath = filePath,
                displayName = filePath.fileName.toString(),
                codeArea = codeArea
            )

            tabData.isDirtyListener = ChangeListener { _, _, isDirty ->
                if (isDirty) {
                    dirtyFiles.add(tabData.filePath)
                } else {
                    dirtyFiles.remove(tabData.filePath)
                }
            }.also { tabData.isDirty.addListener(it) }

            tabData.textListener = InvalidationListener {
                if (!tabData.isDirty.get()) {
                    tabData.isDirty.set(true)
                }
            }.also { codeArea.textProperty().addListener(it) }

            // Listen for caret position changes to update line and column numbers
            tabData.caretListener = InvalidationListener {
                updateLineAndColumn(codeArea)
            }.also { codeArea.caretPositionProperty().addListener(it) }

            tabs.add(tabData)
            activeTab.set(tabData)

            logger.debug("Tab created for file: {}", filePath.fileName)

        } catch (e: Exception) {
            logger.error("Failed to create tab for file: ${filePath.fileName}", e)
        }
    }

    /**
     * Updates the line and column properties based on the caret position in the given CodeArea.
     * If the codeArea is null (e.g., no active tab), it resets the values.
     */
    fun updateLineAndColumn(codeArea: CodeArea?) {
        if (codeArea != null) {
            val line = codeArea.currentParagraph + 1
            val column = codeArea.caretColumn + 1
            currentLine.set(line)
            currentColumn.set(column)
            BottomBarModule.updateCursorPosition(line, column)
        } else {
            currentLine.set(1)
            currentColumn.set(1)
        }
    }
    /**
     * Closes the specified tab and auto-saves before closing
     */
    fun closeTab(tabData: TabData) {
        logger.debug("Closing tab: ${tabData.displayName}")

        // Auto-save before closing
        try {
            saveFile(tabData)
            logger.debug("Auto-saved file before closing: ${tabData.displayName}")
        } catch (e: Exception) {
            logger.error("Failed to auto-save before closing: ${tabData.displayName}", e)
        }

        // Remove listeners to prevent memory leaks
        tabData.isDirtyListener?.let { tabData.isDirty.removeListener(it) }
        tabData.textListener?.let { tabData.codeArea.textProperty().removeListener(it) }
        tabData.caretListener?.let { tabData.codeArea.caretPositionProperty().removeListener(it) }
        tabData.isDirtyListener = null
        tabData.textListener = null
        tabData.caretListener = null

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
        saveFile(tab)
        logger.info("Manually saved file: ${tab.displayName}")
    }

    private fun saveFile(tabData: TabData) {
        try {
            val content = tabData.codeArea.text
            tabData.filePath.writeText(content)
            tabData.isDirty.set(false)
            logger.debug("File saved: {} ({} characters)", tabData.filePath.fileName, content.length)
        } catch (e: Exception) {
            logger.error("Failed to save file: ${tabData.filePath.fileName}", e)
            // TODO: Show error notification to user
        }
    }

    /**
     * Cleanup method to be called when the editor is closed
     */
    fun cleanup() {
        // Auto-save all tabs before cleanup
        tabs.forEach { tab ->
            try {
                saveFile(tab)
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