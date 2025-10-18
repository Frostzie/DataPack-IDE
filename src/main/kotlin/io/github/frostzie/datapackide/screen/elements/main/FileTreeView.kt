package io.github.frostzie.datapackide.screen.elements.main

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MoveFile
import io.github.frostzie.datapackide.events.OpenFile
import io.github.frostzie.datapackide.modules.main.FileTreeViewModel
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.input.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.nio.file.Path
import kotlin.io.path.isDirectory

/**
 * The View for the file tree. This class is responsible for displaying the tree.
 */
class FileTreeView : VBox() {
    private val viewModel = FileTreeViewModel()
    private val treeView = TreeView<FileTreeItem>()

    // A custom DataFormat used to identify drag-and-drop operations initiated from this file tree.
    // This ensures that the tree only handles drops that it originated, preventing conflicts with
    // other drag-and-drop sources. The string is a unique identifier, conventionally using a package name format.
    private val dragDataFormat = DataFormat("io.github.frostzie.datapackide.FileTreeItem")

    init {
        styleClass.add("file-tree-container")

        prefWidth = UIConstants.FILE_TREE_DEFAULT_WIDTH
        minWidth = UIConstants.FILE_TREE_MIN_WIDTH
        setVgrow(treeView, Priority.ALWAYS)
        children.add(treeView)

        treeView.rootProperty().bind(viewModel.root)

        treeView.isShowRoot = false

        // A cell factory is used to customize each cell in the tree. This includes setting up
        // mouse click listeners for opening files and handling all drag-and-drop gestures.
        treeView.setCellFactory { _ ->
            object : TreeCell<FileTreeItem>() {
                init {
                    setOnMouseClicked { event ->
                        if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                            val currentItem = item ?: return@setOnMouseClicked
                            if (!currentItem.path.isDirectory()) {
                                EventBus.post(OpenFile(currentItem.path))
                            }
                        }
                    }

                    setOnDragDetected { event ->
                        if (item == null) return@setOnDragDetected
                        val db = startDragAndDrop(TransferMode.MOVE)
                        val content = ClipboardContent()
                        content[dragDataFormat] = item.path.toString()
                        db.setContent(content)
                        event.consume()
                    }

                    setOnDragOver { event ->
                        if (isValidDropTarget(event)) {
                            event.acceptTransferModes(TransferMode.MOVE)
                        }
                        event.consume()
                    }

                    setOnDragEntered { event ->
                        if (isValidDropTarget(event)) {
                            styleClass.add("drag-over")
                        }
                    }

                    setOnDragExited {
                        styleClass.remove("drag-over")
                    }

                    setOnDragDropped { event ->
                        styleClass.remove("drag-over")
                        val db = event.dragboard
                        var success = false
                        if (db.hasContent(dragDataFormat)) {
                            val targetItem = item ?: return@setOnDragDropped
                            val sourcePath = Path.of(db.getContent(dragDataFormat) as String)
                            val targetPath = targetItem.path.resolve(sourcePath.fileName)

                            EventBus.post(MoveFile(sourcePath, targetPath))
                            success = true
                        }
                        event.isDropCompleted = success
                        event.consume()
                    }
                }

                override fun updateItem(item: FileTreeItem?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty) null else item?.toString()
                    graphic = null // Can add icons here later
                }

                private fun isValidDropTarget(event: DragEvent): Boolean {
                    if (event.gestureSource == this || !event.dragboard.hasContent(dragDataFormat)) {
                        return false
                    }
                    val targetItem = item ?: return false
                    val sourcePath = Path.of(event.dragboard.getContent(dragDataFormat) as String)

                    // Valid if the target is a directory, not the source itself, and not a child of the source
                    return targetItem.path.isDirectory() && sourcePath != targetItem.path && !targetItem.path.startsWith(sourcePath)
                }
            }
        }
    }
}
