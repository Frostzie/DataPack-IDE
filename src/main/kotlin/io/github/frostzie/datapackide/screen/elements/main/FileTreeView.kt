package io.github.frostzie.datapackide.screen.elements.main

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.FileOpenEvent
import io.github.frostzie.datapackide.events.DirectorySelectedEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.utils.ComponentResizer
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.scene.control.*
import javafx.scene.layout.VBox
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Simple file tree view that displays files and folders
 */
class FileTreeView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("FileTreeView")
    }

    private lateinit var treeView: TreeView<File>
    private var currentDirectory: Path? = null

    init {
        setupFileTree()
        setupEventListeners()
        ComponentResizer.install(this, UIConstants.FILE_TREE_RESIZER_WIDTH, UIConstants.FILE_TREE_MIN_WIDTH, UIConstants.FILE_TREE_MAX_WIDTH)
        logger.info("File tree view initialized")
    }

    private fun setupFileTree() {
        styleClass.add("file-tree-container")
        CSSManager.applyToComponent(stylesheets, "FileTree")

        prefWidth = UIConstants.FILE_TREE_DEFAULT_WIDTH
        minWidth = UIConstants.FILE_TREE_MIN_WIDTH
        maxWidth = UIConstants.FILE_TREE_MAX_WIDTH

        prefHeight = USE_COMPUTED_SIZE
        maxHeight = Double.MAX_VALUE
        minHeight = 0.0

        treeView = TreeView<File>().apply {
            styleClass.add("file-tree")
            isShowRoot = false

            prefHeight = USE_COMPUTED_SIZE
            maxHeight = Double.MAX_VALUE
            minHeight = 0.0

            setCellFactory {
                object : TreeCell<File>() {
                    override fun updateItem(item: File?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) null else item.name

                        graphic = null
                        if (!empty && item != null) {
                            styleClass.removeAll("file-item", "folder-item")
                            styleClass.add(if (item.isDirectory) "folder-item" else "file-item")
                        }
                    }
                }
            }

            setOnMouseClicked { event ->
                if (event.clickCount == 2) {
                    val selectedItem = selectionModel.selectedItem
                    if (selectedItem != null && !selectedItem.value.isDirectory) {
                        val file = selectedItem.value
                        logger.info("File double-clicked: ${file.name}")
                        EventBus.post(FileOpenEvent(file.toPath()))
                    }
                }
            }
        }

        val placeholderLabel = Label("No directory selected.\nClick the folder icon to select a directory.").apply {
            styleClass.add("file-tree-placeholder")
            isWrapText = true
            prefHeight = USE_COMPUTED_SIZE
            maxHeight = Double.MAX_VALUE
        }

        treeView.cursorProperty().bind(this.cursorProperty())
        placeholderLabel.cursorProperty().bind(this.cursorProperty())

        children.addAll(placeholderLabel, treeView)

        setVgrow(treeView, javafx.scene.layout.Priority.ALWAYS)
        setVgrow(placeholderLabel, javafx.scene.layout.Priority.ALWAYS)

        treeView.isVisible = false
        treeView.isManaged = false
    }

    private fun setupEventListeners() {
        EventBus.register<DirectorySelectedEvent> { event ->
            loadDirectory(event.directoryPath)
        }
    }

    private fun loadDirectory(directoryPath: Path) {
        try {
            if (!directoryPath.exists() || !directoryPath.isDirectory()) {
                logger.warn("Invalid directory path: $directoryPath")
                return
            }

            currentDirectory = directoryPath
            logger.info("Loading directory: $directoryPath")

            val rootFile = directoryPath.toFile()
            val rootItem = TreeItem(rootFile)

            loadDirectoryContents(rootItem, rootFile)

            treeView.root = rootItem

            children[0].isVisible = false
            children[0].isManaged = false
            treeView.isVisible = true
            treeView.isManaged = true

            rootItem.isExpanded = true

            logger.info("Directory loaded successfully: ${rootFile.name}")

        } catch (e: Exception) {
            logger.error("Failed to load directory: $directoryPath", e)
        }
    }

    private fun loadDirectoryContents(parentItem: TreeItem<File>, directory: File) {
        try {
            val files = directory.listFiles()?.sortedWith(
                compareBy<File> { !it.isDirectory() }.thenBy { it.name.lowercase() }
            ) ?: return

            for (file in files) {
                if (file.name.startsWith(".")) continue

                val childItem = TreeItem(file)
                parentItem.children.add(childItem)

                if (file.isDirectory()) {
                    childItem.children.add(TreeItem(File("Loading...")))

                    childItem.expandedProperty().addListener { _, _, isExpanded ->
                        if (isExpanded && childItem.children.size == 1 &&
                            childItem.children[0].value.name == "Loading...") {
                            childItem.children.clear()
                            loadDirectoryContents(childItem, file)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load directory contents: ${directory.absolutePath}", e)
        }
    }

    fun getCurrentDirectory(): Path? = currentDirectory
}