package io.github.frostzie.datapackide.screen.elements.main

import io.github.frostzie.datapackide.config.WebsiteConfig
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.FileOpenEvent
import io.github.frostzie.datapackide.events.EditorContentChangedEvent
import io.github.frostzie.datapackide.events.EditorCursorChangedEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.beans.property.SimpleStringProperty
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import java.nio.file.Path
import kotlin.io.path.readText

/**
 * WebView-based text editor that renders the website from config folder
 */
class TextEditor : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TextEditor")
    }

    val filePathProperty = SimpleStringProperty("")
    val modifiedProperty = SimpleStringProperty("")
    val lineCountProperty = SimpleStringProperty("Lines: 1")

    private lateinit var webView: WebView
    private var currentFilePath: String? = null
    private var isModified: Boolean = false

    init {
        setupWebViewEditor()
        setupEventListeners()
        logger.info("WebView text editor initialized")
    }

    private fun setupWebViewEditor() {
        styleClass.add("text-editor-container")
        CSSManager.applyToComponent(stylesheets, "TextEditor")

        webView = WebView().apply {
            styleClass.add("main-webview")
            val htmlPath = WebsiteConfig.getWebsiteIndexPath()

            if (htmlPath.toFile().exists()) {
                engine.load(htmlPath.toUri().toString())
                logger.info("Loading editor website from config: $htmlPath")
            } else {
                engine.loadContent("<html><body><h3>Error: Editor website not found in config</h3></body></html>")
                logger.error("Editor website not found in config directory: $htmlPath")
            }

            engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState == Worker.State.SUCCEEDED) {
                    setupJavaScriptBridge()
                    logger.debug("WebView loaded successfully")
                }
            }
        }

        children.add(webView)
        setVgrow(webView, Priority.ALWAYS)

        updateLineCount()
        filePathProperty.set("Untitled")
    }

    private fun setupEventListeners() {
        EventBus.register<FileOpenEvent> { event ->
            openFileInWebView(event.filePath)
        }
    }

    private fun setupJavaScriptBridge() {
        try {
            val window = webView.engine.executeScript("window") as JSObject
            window.setMember("javaConnector", EditorBridge(this))
            logger.debug("JavaScript bridge established")
        } catch (e: Exception) {
            logger.error("Failed to set up JavaScript bridge", e)
        }
    }

    private fun openFileInWebView(filePath: Path) {
        try {
            val content = filePath.readText()
            currentFilePath = filePath.toString()
            isModified = false

            filePathProperty.set(filePath.toString())
            updateModifiedStatus()
            updateLineCount(content)

            (webView.engine.executeScript("window") as? JSObject)?.call("editorSetContent", content, filePath.toString())
            logger.info("File opened in WebView: ${filePath.fileName}")
        } catch (e: Exception) {
            logger.error("Failed to open file in WebView: $filePath", e)
        }
    }

    private fun updateModifiedStatus() {
        val status = if (isModified) "●" else ""
        modifiedProperty.set(status)

        val fileName = currentFilePath?.substringAfterLast("/") ?: "Untitled"
        val displayName = if (isModified) "$fileName ●" else fileName
        logger.debug("File modified status updated: $displayName")
    }

    private fun updateLineCount(content: String? = null) {
        val text = content ?: getContentFromWebView()
        val lineCount = if (text.isEmpty()) 1 else text.count { it == '\n' } + 1
        lineCountProperty.set("Lines: $lineCount")
    }

    private fun getContentFromWebView(): String {
        return try {
            val result = webView.engine.executeScript("window.editorGetContent && window.editorGetContent() || '';")
            result?.toString() ?: ""
        } catch (e: Exception) {
            logger.error("Failed to get content from WebView", e)
            ""
        }
    }

    var onCursorPositionChanged: ((line: Int, column: Int) -> Unit)? = null

    inner class EditorBridge(private val editor: TextEditor) {
        fun editorReady() {
            logger.info("CodeMirror editor is ready.")
            Platform.runLater {
                editor.requestFocus()
            }
        }

        fun contentChanged(content: String) {
            Platform.runLater {
                if (!isModified) {
                    isModified = true
                    updateModifiedStatus()
                }
                updateLineCount(content)

                EventBus.post(EditorContentChangedEvent(content, currentFilePath))
            }
        }

        fun cursorPositionChanged(line: Int, column: Int) {
            Platform.runLater {
                logger.info("EditorBridge received cursor change: Ln $line, Col $column")
                onCursorPositionChanged?.invoke(line, column)

                EventBus.post(EditorCursorChangedEvent(line, column, currentFilePath))
            }
        }
    }

    fun newFile() {
        (webView.engine.executeScript("window") as? JSObject)?.call("editorSetContent", "")
        currentFilePath = null
        isModified = false
        filePathProperty.set("Untitled")
        updateModifiedStatus()
        updateLineCount("")
        logger.info("New file created")
    }

    fun setText(content: String, filePath: String? = null) {
        (webView.engine.executeScript("window") as? JSObject)?.call("editorSetContent", content, filePath)
        currentFilePath = filePath
        isModified = false
        filePathProperty.set(filePath ?: "Untitled")
        updateModifiedStatus()
        updateLineCount(content)
        logger.info("Text set for file: ${filePath ?: "Untitled"}")
    }

    fun markAsSaved() {
        isModified = false
        updateModifiedStatus()
        logger.debug("File marked as saved")
    }

    fun getText(): String {
        return getContentFromWebView()
    }

    fun insertText(text: String) {
        (webView.engine.executeScript("window") as? JSObject)?.call("editorInsertText", text)
    }

    fun getSelectedText(): String {
        return try {
            val result = webView.engine.executeScript("window.editorGetSelectedText && window.editorGetSelectedText() || '';")
            result?.toString() ?: ""
        } catch (e: Exception) {
            logger.error("Failed to get selected text from WebView", e)
            ""
        }
    }

    fun cut() {
        webView.engine.executeScript("window.editorCut && window.editorCut();")
    }

    fun copy() {
        webView.engine.executeScript("window.editorCopy && window.editorCopy();")
    }

    fun paste() {
        webView.engine.executeScript("window.editorPaste && window.editorPaste();")
    }

    fun undo() {
        webView.engine.executeScript("window.editorUndo && window.editorUndo();")
    }

    fun redo() {
        webView.engine.executeScript("window.editorRedo && window.editorRedo();")
    }

    fun selectAll() {
        webView.engine.executeScript("window.editorSelectAll && window.editorSelectAll();")
    }

    fun find(searchText: String): Boolean {
        return try {
            val result = (webView.engine.executeScript("window") as? JSObject)?.call("editorFind", searchText)
            result as? Boolean ?: false
        } catch (e: Exception) {
            logger.error("Failed to perform find in WebView", e)
            false
        }
    }

    fun getCurrentFilePath(): String? = currentFilePath
    fun isModified(): Boolean = isModified

    fun setEditable(editable: Boolean) {
        webView.engine.executeScript("window.editorSetEditable && window.editorSetEditable($editable);")
    }

    override fun requestFocus() {
        webView.requestFocus()
        webView.engine.executeScript("window.editorFocus && window.editorFocus();")
    }
}