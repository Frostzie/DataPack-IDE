package io.github.frostzie.datapackide.screen.elements.popup

import io.github.frostzie.datapackide.utils.FileUtils
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class NewFileWindow(private val parentStage: Stage?) {

    companion object {
        private val logger = LoggerProvider.getLogger("NewFileWindow")
    }

    private var stage: Stage? = null
    private var xOffset = 0.0
    private var yOffset = 0.0

    private lateinit var fileNameField: TextField
    private lateinit var fileTypeListView: ListView<FileUtils.FileType>

    data class NewFileResult(
        val fileName: String,
        val fileType: FileUtils.FileType
    )

    fun showAndWaitForResult(): NewFileResult? {
        return createAndShowWindow()
    }

    private fun createAndShowWindow(): NewFileResult? {
        stage = Stage().apply {
            initStyle(StageStyle.UNDECORATED)
            initModality(Modality.APPLICATION_MODAL)
            parentStage?.let { initOwner(it) }
            title = "Create New File"
            isResizable = false
        }

        val content = createContent()
        val scene = Scene(content)

        try {
            CSSManager.applyPopupStyles(scene, "NewFileWindow.css")
        } catch (e: Exception) {
            logger.warn("Could not load NewFileWindow CSS: ${e.message}")
        }

        scene.setOnKeyPressed { event ->
            if (event.code == KeyCode.ESCAPE) {
                stage?.close()
            }
        }

        stage?.scene = scene
        stage?.centerOnScreen()
        stage?.showAndWait()
        return stage?.userData as? NewFileResult
    }

    private fun createContent(): VBox {
        val root = VBox().apply {
            styleClass.add("new-file-window")
        }

        val header = createHeader()
        val contentContainer = VBox().apply {
            styleClass.add("content-container")
        }

        val fileNameSection = createFileNameSection()
        val fileTypeSection = createFileTypeSection()

        contentContainer.children.addAll(fileNameSection, fileTypeSection)
        root.children.addAll(header, contentContainer)

        // Window drag
        root.setOnMousePressed { event: MouseEvent ->
            xOffset = event.sceneX
            yOffset = event.sceneY
        }
        root.setOnMouseDragged { event: MouseEvent ->
            stage?.x = event.screenX - xOffset
            stage?.y = event.screenY - yOffset
        }

        return root
    }

    private fun createHeader(): HBox {
        val titleLabel = Label("Create New File").apply {
            styleClass.add("title-label")
        }

        val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

        val closeButton = Button("✕").apply {
            styleClass.add("close-button-popup")
            setOnAction { stage?.close() }
        }

        return HBox(titleLabel, spacer, closeButton).apply {
            styleClass.add("popup-header")
            alignment = Pos.CENTER
        }
    }

    private fun createFileNameSection(): VBox {
        fileNameField = TextField().apply {
            styleClass.add("filename-input")
            promptText = "Enter file name (without extension)"
            requestFocus()
            setOnKeyPressed { event ->
                if (event.code == KeyCode.ENTER) {
                    handleCreateFile()
                }
            }
        }

        return VBox(fileNameField).apply {
            styleClass.add("input-section")
        }
    }

    private fun createFileTypeSection(): VBox {
        val section = VBox().apply {
            styleClass.add("input-section")
        }

        val label = Label("File Type:").apply {
            styleClass.add("input-label")
        }

        fileTypeListView = ListView<FileUtils.FileType>().apply {
            styleClass.add("file-type-list-view")
            items.addAll(FileUtils.FileType.entries)
            isFocusTraversable = false

            fixedCellSize = 28.0
            prefHeight = items.size * fixedCellSize + 2
            maxHeight = prefHeight

            setCellFactory {
                object : ListCell<FileUtils.FileType>() {
                    override fun updateItem(item: FileUtils.FileType?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) null else "${item.displayName} (.${item.extension})"
                    }
                }
            }
            selectionModel.selectFirst()
        }

        section.children.addAll(label, fileTypeListView)
        return section
    }

    private fun handleCreateFile() {
        val fileName = fileNameField.text?.trim()
        val selectedType = fileTypeListView.selectionModel.selectedItem

        if (fileName.isNullOrBlank()) {
            showError("Please enter a file name")
            fileNameField.requestFocus()
            return
        }

        if (selectedType == null) {
            showError("Please select a file type")
            fileTypeListView.requestFocus()
            return
        }

        if (!isValidFileName(fileName)) {
            showError("Invalid file name. Only letters, numbers, underscores, and hyphens are allowed.")
            fileNameField.requestFocus()
            return
        }

        val result = NewFileResult(fileName, selectedType)
        stage?.userData = result
        logger.info("File creation confirmed: $fileName.${selectedType.extension}")
        stage?.close()
    }

    private fun showError(message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            title = "Invalid Input"
            headerText = null
            contentText = message
            stage?.let { initOwner(it) }
            showAndWait()
        }
    }

    private fun isValidFileName(fileName: String): Boolean {
        val validPattern = Regex("^[a-zA-Z0-9_\\-\\s]+\$")
        return fileName.matches(validPattern) && fileName.isNotBlank()
    }
}