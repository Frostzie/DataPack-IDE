package io.github.frostzie.datapackide.screen.elements.main

import io.github.frostzie.datapackide.eventsOLD.EventBusOLD
import io.github.frostzie.datapackide.eventsOLD.NodeSelectionRequestEvent
import io.github.frostzie.datapackide.eventsOLD.FileActionEvent
import io.github.frostzie.datapackide.eventsOLD.FileAction
import io.github.frostzie.datapackide.eventsOLD.FileTreeDragStartEvent
import io.github.frostzie.datapackide.eventsOLD.FileTreeDragEndEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.geometry.Insets
import javafx.scene.input.ClipboardContent
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import javafx.scene.layout.StackPane
import java.io.File

/**
 * Individual file tree node representing a file or directory
 */
class FileTreeNode(
    private val file: File,
    val depth: Int,
    private val isDirectory: Boolean,
    private var isExpanded: Boolean = false,
    displayName: String? = null,
    originalFile: File
) : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("FileTreeNode")
        private const val ITEM_HEIGHT = 22.0
        private const val INDENT_SIZE = 22.0
    }

    private val expandArrowContainer: StackPane?
    private val expandArrow: Label?
    private val iconLabel: Label
    private val nameLabel: Label
    private var onExpandToggle: ((Boolean) -> Unit)? = null
    private var onDoubleClick: (() -> Unit)? = null
    private var clickCount = 0
    private var lastClickTime = 0L
    private var isSelected = false
    private var isDragSource = false
    private val config get() = AdvancedConfig.enableDebugMode.value && AdvancedConfig.debugTreeViewHitbox.value

    init {
        styleClass.add("file-tree-node")
        if (isDirectory) styleClass.add("directory-node") else styleClass.add("file-node")

        prefHeight = ITEM_HEIGHT
        minHeight = ITEM_HEIGHT
        maxHeight = ITEM_HEIGHT
        minWidth = 0.0 // Prevent the HBox from recalculating child positions on shrink

        padding = Insets(0.0, 5.0, 0.0, depth * INDENT_SIZE)
        spacing = 5.0
        alignment = javafx.geometry.Pos.CENTER_LEFT

        // Create expand arrow for directories
        if (isDirectory) {
            expandArrowContainer = StackPane().apply {
                styleClass.add("expand-arrow-container")
                prefWidth = ITEM_HEIGHT
                prefHeight = ITEM_HEIGHT
                minWidth = ITEM_HEIGHT
                minHeight = ITEM_HEIGHT
                maxWidth = ITEM_HEIGHT
                maxHeight = ITEM_HEIGHT

                setOnMouseClicked { event ->
                    if (event.button == MouseButton.PRIMARY) {
                        event.consume()
                        toggleExpansion()
                    }
                }
            }

            expandArrow = Label(if (isExpanded) "v" else ">").apply {
                styleClass.add("expand-arrow")
                alignment = javafx.geometry.Pos.CENTER
            }

            expandArrowContainer.children.add(expandArrow)
        } else {
            expandArrowContainer = null
            expandArrow = null
        }

        // Create icon
        iconLabel = Label().apply {
            styleClass.add("file-icon")
            if (isDirectory) {
                text = "📁" // Placeholder folder icon
                styleClass.add("directory-icon")
            } else {
                text = "📄" // Placeholder file icon
                styleClass.add("file-icon-default")
            }
        }

        // Create file/directory name label //TODO: Fix custom file colors
        nameLabel = Label(displayName ?: originalFile.name).apply {
            styleClass.add("file-name-label")
            if (isDirectory) styleClass.add("directory-label") else styleClass.add("file-label")
            prefHeight = ITEM_HEIGHT
            minHeight = ITEM_HEIGHT
            maxHeight = ITEM_HEIGHT
            alignment = javafx.geometry.Pos.CENTER_LEFT
        }

        // Add components
        if (isDirectory) {
            children.add(expandArrowContainer)
        } else {
            val spacer = Region()
            spacer.prefWidth = ITEM_HEIGHT
            children.add(spacer)
        }

        children.add(iconLabel)
        children.add(nameLabel)
        setHgrow(nameLabel, Priority.ALWAYS)

        addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
            handleMouseClick(event)
        }

        setupDragAndDrop()

        setOnMouseEntered {
            if (!isSelected) styleClass.add("hovered")
        }
        setOnMouseExited {
            styleClass.remove("hovered")
        }

        updateDebugVisuals()
    }

    private fun handleMouseClick(event: MouseEvent) {
        if (event.button != MouseButton.PRIMARY) return

        EventBusOLD.post(NodeSelectionRequestEvent(this))

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime < 500) { // Double click threshold
            clickCount++
        } else {
            clickCount = 1
        }

        lastClickTime = currentTime

        if (clickCount >= 2) {
            clickCount = 0
            onDoubleClick?.invoke()
            event.consume()
        }
    }

    fun toggleExpansion() {
        if (!isDirectory) return

        isExpanded = !isExpanded
        updateArrowRotation()
        onExpandToggle?.invoke(isExpanded)
    }

    private fun updateArrowRotation() {
        expandArrow?.text = if (isExpanded) "v" else ">"
    }

    fun setSelected(selected: Boolean) {
        isSelected = selected
        styleClass.remove("hovered")

        if (selected) {
            styleClass.add("selected")
        } else {
            styleClass.remove("selected")
        }
    }

    fun setTreeFocused(isFocused: Boolean) {
        if (isFocused) {
            styleClass.add("tree-focused")
        } else {
            styleClass.remove("tree-focused")
        }
    }

    fun setOnExpandToggle(callback: (Boolean) -> Unit) {
        onExpandToggle = callback
    }

    fun setOnDoubleClick(callback: () -> Unit) {
        onDoubleClick = callback
    }

    private fun setupDragAndDrop() {
        setOnDragDetected { event ->
            if (event.button == MouseButton.PRIMARY) {
                val dragboard = startDragAndDrop(TransferMode.MOVE)
                val content = ClipboardContent()
                content.putString(file.absolutePath)
                dragboard.setContent(content)

                isDragSource = true
                styleClass.add("drag-source")

                setTreeFocused(false)

                EventBusOLD.post(FileTreeDragStartEvent(this))

                logger.debug("Drag started for: ${file.name}")
                event.consume()
            }
        }

        setOnDragOver { event ->
            if (event.gestureSource != this && event.dragboard.hasString()) {
                val sourcePath = event.dragboard.string
                val sourceFile = File(sourcePath)
                val isValidTarget = isDirectory && sourcePath != file.absolutePath && !isChildOf(file, sourceFile)

                if (isValidTarget) {
                    event.acceptTransferModes(TransferMode.MOVE)
                }
            }
            event.consume()
        }

        setOnDragExited { event ->
            event.consume()
        }

        setOnDragDropped { event ->
            var success = false
            if (event.dragboard.hasString() && isDirectory) {
                val sourcePath = event.dragboard.string
                val sourceFile = File(sourcePath)
                val isValidTarget = sourcePath != file.absolutePath && !isChildOf(file, sourceFile)

                if (isValidTarget) {
                    val targetPath = file.toPath().resolve(sourceFile.name)

                    EventBusOLD.post(FileActionEvent(
                        action = FileAction.MOVE,
                        metadata = mapOf(
                            "sourcePath" to sourceFile.toPath(),
                            "targetPath" to targetPath
                        )
                    ))

                    success = true
                }
            }

            event.isDropCompleted = success
            event.consume()
        }

        setOnDragDone { event ->
            isDragSource = false
            styleClass.remove("drag-source")

            (parent?.parent?.parent as? FileTreeView)?.let { treeView ->
                if (treeView.isFocused) setTreeFocused(true)
            }

            EventBusOLD.post(FileTreeDragEndEvent())

            event.consume()
        }
    }

    private fun isChildOf(child: File, potentialParent: File): Boolean {
        var parent = child.parentFile
        while (parent != null) {
            if (parent.absolutePath == potentialParent.absolutePath) {
                return true
            }
            parent = parent.parentFile
        }
        return false
    }

    private fun updateDebugVisuals(sourceNode: FileTreeNode? = null) {
        if (!config) {
            styleClass.removeAll("debug-hitbox", "valid-drag-target", "invalid-drag-target")
            return
        }

        if (!styleClass.contains("debug-hitbox")) {
            styleClass.add("debug-hitbox")
        }

        if (sourceNode != null && this != sourceNode) {
            val isValidTarget = isDirectory && sourceNode.file.absolutePath != file.absolutePath && !isChildOf(file, sourceNode.file)
            if (isValidTarget) {
                styleClass.remove("invalid-drag-target")
                if (!styleClass.contains("valid-drag-target")) styleClass.add("valid-drag-target")
            } else {
                styleClass.remove("valid-drag-target")
                if (!styleClass.contains("invalid-drag-target")) styleClass.add("invalid-drag-target")
            }
        } else {
            styleClass.removeAll("valid-drag-target", "invalid-drag-target")
        }
    }

    fun updateDragTargetStatus(sourceNode: FileTreeNode) {
        updateDebugVisuals(sourceNode)
    }

    fun clearDragTargetStatus() {
        updateDebugVisuals()
    }

    fun getFile(): File = file
    fun isExpanded(): Boolean = isExpanded
}