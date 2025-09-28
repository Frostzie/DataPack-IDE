package io.github.frostzie.datapackide.screen.elements.main

import com.google.common.eventbus.Subscribe
import io.github.frostzie.datapackide.eventsOLD.EventBusOLD
import io.github.frostzie.datapackide.eventsOLD.NodeSelectionRequestEvent
import io.github.frostzie.datapackide.eventsOLD.FileTreeDragStartEvent
import io.github.frostzie.datapackide.eventsOLD.FileTreeDragEndEvent
import io.github.frostzie.datapackide.eventsOLD.FileOpenEvent
import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.eventsOLD.DirectorySelectedEvent
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.ComponentResizer
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Node-based file tree view
 * similar to popular IDEs like IntelliJ IDEA and VSCode
 */
class FileTreeView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("FileTreeView")
        private var currentlySelectedNode: FileTreeNode? = null
    }

    private val contentContainer = VBox()
    private val placeholderLabel = Label("No directory selected.\nClick the folder icon to select a directory.")
    private var currentDirectory: Path? = null
    private val expandedDirectories = mutableSetOf<Path>()

    init {
        setupLayout()
        setupEventListeners()
        ComponentResizer.install(this,
            UIConstants.FILE_TREE_RESIZER_WIDTH,
            UIConstants.FILE_TREE_MIN_WIDTH,
            UIConstants.FILE_TREE_MAX_WIDTH)
        logger.info("Node-based file tree view initialized")

        isFocusTraversable = true
        focusedProperty().addListener { _, _, isFocused ->
            currentlySelectedNode?.setTreeFocused(isFocused)
        }
    }

    private fun setupLayout() {
        styleClass.add("file-tree-container")

        prefWidth = UIConstants.FILE_TREE_DEFAULT_WIDTH
        minWidth = UIConstants.FILE_TREE_MIN_WIDTH
        maxWidth = UIConstants.FILE_TREE_MAX_WIDTH

        prefHeight = USE_COMPUTED_SIZE
        maxHeight = Double.MAX_VALUE
        minHeight = 0.0

        placeholderLabel.apply {
            styleClass.add("file-tree-placeholder")
            isWrapText = true
            prefHeight = USE_COMPUTED_SIZE
            maxHeight = Double.MAX_VALUE
            padding = Insets(20.0)
        }

        contentContainer.apply {
            styleClass.add("file-tree-content")
            prefHeight = USE_COMPUTED_SIZE
            maxHeight = Double.MAX_VALUE
            spacing = 1.0
            prefWidth = UIConstants.FILE_TREE_MAX_WIDTH
        }

        val scrollPane = ScrollPane(contentContainer).apply {
            styleClass.add("file-tree-scroll")
            isFitToWidth = false
            isFitToHeight = false
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            prefViewportHeight = USE_COMPUTED_SIZE
            maxHeight = Double.MAX_VALUE
        }

        children.addAll(placeholderLabel, scrollPane)
        setVgrow(scrollPane, Priority.ALWAYS)
        setVgrow(placeholderLabel, Priority.ALWAYS)

        showPlaceholder()
    }

    private fun setupEventListeners() {
        EventBus.register(this)

        EventBusOLD.register<NodeSelectionRequestEvent> { event ->
            handleNodeSelection(event.node)
        }
        EventBusOLD.register<FileTreeDragStartEvent> { event ->
            updateAllNodesDragStatus(event.sourceNode)
        }
        EventBusOLD.register<FileTreeDragEndEvent> {
            clearAllNodesDragStatus()
        }
    }

    @SubscribeEvent
    fun onDirectorySelected(event: DirectorySelected) {
        loadDirectory(event.directoryPath, preserveState = false)
    }

    private fun showPlaceholder() {
        placeholderLabel.isVisible = true
        placeholderLabel.isManaged = true
        children[1].isVisible = false
        children[1].isManaged = false
    }

    private fun showContent() {
        placeholderLabel.isVisible = false
        placeholderLabel.isManaged = false
        children[1].isVisible = true
        children[1].isManaged = true
    }

    private fun loadDirectory(directoryPath: Path, preserveState: Boolean = false) {
        try {
            if (!directoryPath.exists() || !directoryPath.isDirectory()) {
                logger.warn("Invalid directory path: $directoryPath")
                return
            }

            currentDirectory = directoryPath
            if (!preserveState) {
                expandedDirectories.clear()
            }
            logger.info("Loading directory: $directoryPath")

            contentContainer.children.clear()

            val rootNode = createDirectoryNode(directoryPath.toFile(), directoryPath.toFile(), 0)
            contentContainer.children.add(rootNode)

            if (rootNode.isExpanded()) {
                expandDirectory(rootNode, directoryPath.toFile(), 0)
            }

            showContent()
            logger.info("Directory loaded successfully: ${directoryPath.fileName}")

        } catch (e: Exception) {
            logger.error("Failed to load directory: $directoryPath", e)
        }
    }

    private fun createDirectoryNode(
        directory: File,
        originalFile: File,
        depth: Int,
        displayName: String? = null
    ): FileTreeNode {
        val isExpanded = expandedDirectories.contains(originalFile.toPath())
        val node = FileTreeNode(directory, depth, true, isExpanded, displayName, originalFile)

        node.setOnExpandToggle { expanded ->
            if (expanded) {
                expandDirectory(node, directory, depth)
                expandedDirectories.add(directory.toPath())
            } else {
                collapseDirectory(node)
                expandedDirectories.remove(directory.toPath())
            }
        }

        node.setOnDoubleClick {
            node.toggleExpansion()
        }

        return node
    }

    private fun createFileNode(file: File, depth: Int): FileTreeNode {
        val node = FileTreeNode(file, depth, false, false, null, file)

        node.setOnDoubleClick {
            logger.info("File double-clicked: ${file.name}")
            EventBusOLD.post(FileOpenEvent(file.toPath()))
        }

        return node
    }

    private fun getCompactableChild(directory: File): File? {
        val children = directory.listFiles()?.filter { !it.name.startsWith(".") }
        if (children != null && children.size == 1) {
            val singleChild = children.first()
            if (singleChild.isDirectory) {
                return singleChild
            }
        }
        return null
    }

    private fun expandDirectory(parentNode: FileTreeNode, directory: File, depth: Int) {
        try {
            val files = directory.listFiles()?.sortedWith(
                compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
            ) ?: return

            val nodesToAdd = mutableListOf<FileTreeNode>()
            val processedFiles = mutableSetOf<File>()

            for (file in files) {
                if (file.name.startsWith(".") || file in processedFiles) continue

                if (file.isDirectory) {
                    var currentFile = file
                    var compactedName = file.name
                    val chain = mutableListOf(currentFile)

                    var compactableChild = getCompactableChild(currentFile)
                    while (compactableChild != null) {
                        currentFile = compactableChild
                        compactedName += ".${currentFile.name}"
                        chain.add(currentFile)
                        compactableChild = getCompactableChild(currentFile)
                    }

                    val node = createDirectoryNode(currentFile, file, depth + 1, if (chain.size > 1) compactedName else null)
                    nodesToAdd.add(node)
                    processedFiles.addAll(chain)
                } else {
                    nodesToAdd.add(createFileNode(file, depth + 1))
                }
            }

            val parentIndex = contentContainer.children.indexOf(parentNode)
            if (parentIndex != -1) {
                contentContainer.children.addAll(parentIndex + 1, nodesToAdd)
            }

        } catch (e: Exception) {
            logger.error("Failed to expand directory: ${directory.absolutePath}", e)
        }
    }

    private fun collapseDirectory(parentNode: FileTreeNode) {
        val parentIndex = contentContainer.children.indexOf(parentNode)
        val childrenToRemove = mutableListOf<FileTreeNode>()

        for (i in (parentIndex + 1) until contentContainer.children.size) {
            val child = contentContainer.children[i] as? FileTreeNode ?: break
            if (child.depth <= parentNode.depth) break
            childrenToRemove.add(child)
        }

        contentContainer.children.removeAll(childrenToRemove)
    }

    private fun getExpandedChildrenCount(parentNode: FileTreeNode): Int {
        val parentIndex = contentContainer.children.indexOf(parentNode)
        var count = 0

        for (i in (parentIndex + 1) until contentContainer.children.size) {
            val child = contentContainer.children[i] as? FileTreeNode ?: break
            if (child.depth <= parentNode.depth) break
            count++
        }

        return count
    }

    fun getCurrentDirectory(): Path? = currentDirectory

    fun refreshDirectory() {
        currentDirectory?.let { loadDirectory(it, preserveState = true) }
    }

    private fun handleNodeSelection(node: FileTreeNode) {
        if (!isFocused || node != currentlySelectedNode) {
            requestFocus()
        }

        if (node == currentlySelectedNode) return

        currentlySelectedNode?.setSelected(false)

        node.setSelected(true)
        currentlySelectedNode = node
        node.setTreeFocused(true)
    }

    private fun updateAllNodesDragStatus(sourceNode: FileTreeNode) {
        contentContainer.children.forEach { node ->
            if (node is FileTreeNode) {
                node.updateDragTargetStatus(sourceNode)
            }
        }
    }

    private fun clearAllNodesDragStatus() {
        contentContainer.children.forEach { node ->
            (node as? FileTreeNode)?.clearDragTargetStatus()
        }
    }
}