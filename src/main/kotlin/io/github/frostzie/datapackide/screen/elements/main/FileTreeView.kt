package io.github.frostzie.datapackide.screen.elements.main

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.OpenFile
import io.github.frostzie.datapackide.modules.main.FileTreeViewModel
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.io.path.isDirectory

/**
 * The View for the file tree. This class is responsible for displaying the tree.
 */
class FileTreeView : VBox() {
    private val viewModel = FileTreeViewModel()
    private val treeView = TreeView<FileTreeItem>()

    init {
        styleClass.add("file-tree-container")

        prefWidth = UIConstants.FILE_TREE_DEFAULT_WIDTH
        minWidth = UIConstants.FILE_TREE_MIN_WIDTH
        setVgrow(treeView, Priority.ALWAYS)
        children.add(treeView)

        treeView.rootProperty().bind(viewModel.root)

        treeView.isShowRoot = false

        // Use a cell factory to customize each cell's appearance and behavior.
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
                }

                override fun updateItem(item: FileTreeItem?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty) null else item?.toString()
                    graphic = null // Can add icons here later
                }
            }
        }
    }
}
