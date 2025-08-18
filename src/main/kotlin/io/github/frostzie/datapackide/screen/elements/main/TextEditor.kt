package io.github.frostzie.datapackide.screen.elements

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * Main text editor component that will later support RichTextFX.
 * Currently, uses a simple TextArea.
 */
class TextEditor : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TextEditor")
    }

    val filePathProperty = SimpleStringProperty("")
    val modifiedProperty = SimpleStringProperty("")
    val lineCountProperty = SimpleStringProperty("Lines: 1")

    private lateinit var textArea: TextArea
    private lateinit var scrollPane: ScrollPane
    private var currentFilePath: String? = null
    private var isModified: Boolean = false

    init {
        setupTextEditor()
        setupEventHandlers()
        logger.info("Text editor initialized")
    }

    private fun setupTextEditor() {
        styleClass.add("text-editor-container")
        stylesheets.add(javaClass.getResource("/assets/datapack-ide/themes/TextEditor.css")?.toExternalForm())

        textArea = TextArea().apply {
            styleClass.add("main-text-area")
            isWrapText = false
            text = ""
        }

        scrollPane = ScrollPane(textArea).apply {
            styleClass.add("text-editor-scroll")
            isFitToWidth = true
            isFitToHeight = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }

        children.add(scrollPane)
        VBox.setVgrow(scrollPane, Priority.ALWAYS)

        updateLineCount()
        filePathProperty.set("Untitled")
    }

    private fun setupEventHandlers() {
        textArea.textProperty().addListener { _, _, newText ->
            if (!isModified) {
                isModified = true
                updateModifiedStatus()
            }
            updateLineCount()
        }

        textArea.caretPositionProperty().addListener { _, _, _ ->
            updateCursorPosition()
        }

        textArea.setOnKeyPressed { event ->
            logger.debug("Key pressed: {}", event.code)
        }
    }

    private fun updateModifiedStatus() {
        val status = if (isModified) "●" else ""
        modifiedProperty.set(status)

        val fileName = currentFilePath?.substringAfterLast("/") ?: "Untitled"
        val displayName = if (isModified) "$fileName ●" else fileName
        logger.debug("File modified status updated: $displayName")
    }

    private fun updateLineCount() {
        val text = textArea.text ?: ""
        val lineCount = if (text.isEmpty()) 1 else text.count { it == '\n' } + 1
        lineCountProperty.set("Lines: $lineCount")
    }

    private fun updateCursorPosition() {
        val caretPos = textArea.caretPosition
        val text = textArea.text ?: ""

        if (caretPos >= 0 && caretPos <= text.length) {
            val beforeCaret = text.substring(0, caretPos)
            val line = beforeCaret.count { it == '\n' } + 1
            val lastNewline = beforeCaret.lastIndexOf('\n')
            val column = if (lastNewline == -1) caretPos + 1 else caretPos - lastNewline

            logger.debug("Cursor position: Line $line, Column $column")
            onCursorPositionChanged?.invoke(line, column)
        }
    }

    var onCursorPositionChanged: ((line: Int, column: Int) -> Unit)? = null

    fun newFile() {
        textArea.text = ""
        currentFilePath = null
        isModified = false
        filePathProperty.set("Untitled")
        updateModifiedStatus()
        updateLineCount()
        logger.info("New file created")
    }

    fun setText(content: String, filePath: String? = null) {
        textArea.text = content
        currentFilePath = filePath
        isModified = false
        filePathProperty.set(filePath ?: "Untitled")
        updateModifiedStatus()
        updateLineCount()
        logger.info("Text set for file: ${filePath ?: "Untitled"}")
    }

    fun markAsSaved() {
        isModified = false
        updateModifiedStatus()
        logger.debug("File marked as saved")
    }

    fun getText(): String {
        return textArea.text ?: ""
    }

    fun insertText(text: String) {
        val caretPos = textArea.caretPosition
        textArea.insertText(caretPos, text)
    }

    fun getSelectedText(): String {
        return textArea.selectedText ?: ""
    }

    fun cut() {
        textArea.cut()
    }

    fun copy() {
        textArea.copy()
    }

    fun paste() {
        textArea.paste()
    }

    fun undo() {
        textArea.undo()
    }

    fun redo() {
        textArea.redo()
    }

    fun selectAll() {
        textArea.selectAll()
    }

    fun find(searchText: String): Boolean {
        val text = textArea.text?.lowercase() ?: return false
        val search = searchText.lowercase()
        val index = text.indexOf(search, textArea.caretPosition)

        if (index != -1) {
            textArea.selectRange(index, index + searchText.length)
            return true
        }
        return false
    }

    fun getCurrentFilePath(): String? = currentFilePath
    fun isModified(): Boolean = isModified

    // Methods to prepare for RichTextFX migration
    fun setEditable(editable: Boolean) {
        textArea.isEditable = editable
    }

    override fun requestFocus() {
        textArea.requestFocus()
    }
}