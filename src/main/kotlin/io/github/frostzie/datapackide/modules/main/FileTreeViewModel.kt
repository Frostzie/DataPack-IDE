package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MoveFile
import io.github.frostzie.datapackide.events.WorkspaceUpdated
import io.github.frostzie.datapackide.project.Project
import io.github.frostzie.datapackide.project.WorkspaceManager
import io.github.frostzie.datapackide.screen.elements.main.FileTreeItem
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.file.FileSystemWatcher
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class FileTreeViewModel {
    private val logger = LoggerProvider.getLogger("FileTreeViewModel")
    
    // The invisible root of the TreeView. Its children are the Project roots.
    val root = SimpleObjectProperty<TreeItem<FileTreeItem>>()
    
    // Map of a project path to its FileSystemWatcher
    private val watchers = mutableMapOf<Path, FileSystemWatcher>()
    private val watchersLock = Any()
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Track expanded paths for persistence
    private val expandedPaths = mutableSetOf<Path>()
    private var isRestoringExpansion = false

    init {
        val dummyRoot = TreeItem(FileTreeItem(Paths.get("Workspace"), "Workspace"))
        dummyRoot.isExpanded = true
        root.set(dummyRoot)

        EventBus.register(this)
        
        // Initial load if workspace is already ready
        updateWorkspace(WorkspaceManager.workspace.projects)
    }

    fun setWindowFocused(focused: Boolean) {
        synchronized(watchersLock) {
            watchers.values.forEach { it.setWindowFocused(focused) }
        }
    }

    fun cleanup() {
        scope.cancel()
        synchronized(watchersLock) {
            watchers.values.forEach { it.stop() }
            watchers.clear()
        }
        EventBus.unregister(this)
    }

    /**
     * Handles the "Open Folder" action.
     */
    @Suppress("unused")
    @SubscribeEvent
    fun onDirectorySelected(event: DirectorySelected) {
        logger.info("Opening single directory as project: ${event.directoryPath}")
        WorkspaceManager.openSingleProject(event.directoryPath)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWorkspaceUpdated(event: WorkspaceUpdated) {
        updateWorkspace(event.workspace.projects)
    }

    private fun updateWorkspace(projects: List<Project>) {
        // Run on background thread to avoid blocking UI with IO or watcher setup
        scope.launch {
            synchronized(watchersLock) {
                // 1. Identify removed projects
                val currentPaths = watchers.keys.toSet()
                val newPaths = projects.map { it.path }.toSet()
                
                val toRemove = currentPaths - newPaths
                val toAdd = newPaths - currentPaths

                // Remove watchers for closed projects
                toRemove.forEach { path ->
                    watchers[path]?.stop()
                    watchers.remove(path)
                }

                // Add watchers for new projects
                toAdd.forEach { path ->
                    val watcher = FileSystemWatcher(path)
                    watcher.start()
                    watchers[path] = watcher
                }
            }
            
            // Load saved expansion state
            val state = WorkspaceManager.getCurrentState()
            isRestoringExpansion = true
            expandedPaths.clear()
            expandedPaths.addAll(state.expandedPaths)

            // 2. Update the UI Tree
            Platform.runLater {
                val rootNode = root.get()
                
                // Remove nodes that are no longer in the project list
                rootNode.children.removeIf { item ->
                    val path = item.value?.path
                    path != null && projects.none { it.path == path }
                }

                // Add or Update nodes
                projects.forEach { project ->
                    val existingNode = rootNode.children.find { it.value?.path == project.path }
                    if (existingNode == null) {
                        // Create new project node
                        val projectNode = TreeItem(FileTreeItem(project.path, project.name))
                        projectNode.isExpanded = true // Auto-expand project roots
                        
                        // Load children asynchronously
                        scope.launch {
                            val children = loadChildren(project.path)
                            Platform.runLater {
                                projectNode.children.setAll(children)
                                restoreExpandedPaths(projectNode) // Try to restore
                            }
                        }
                        rootNode.children.add(projectNode)
                        
                        // Add listener to project root itself
                        addExpansionListener(projectNode, project.path)
                    }
                }
                isRestoringExpansion = false
            }
        }
    }
    
    private fun restoreExpandedPaths(node: TreeItem<FileTreeItem>) {
        if (node.value == null) return
        
        // Check children
        node.children.forEach { child ->
            val childPath = child.value?.path
            if (childPath != null && childPath in expandedPaths) {
                child.isExpanded = true
                restoreExpandedPaths(child)
            }
        }
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onFileMoved(event: MoveFile) {
        logger.info("Moving file from ${event.sourcePath} to ${event.targetPath}")
        try {
            try {
                Files.move(event.sourcePath, event.targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                logger.warn("Atomic move not supported, falling back to standard move.")
                Files.move(event.sourcePath, event.targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            
            // Reload the parent of the source and target to reflect changes
            // Note: The FileWatcher should trigger this update.
            // For now, we manually trigger a refresh if we can find the parent node.
             refreshNode(event.sourcePath.parent)
             refreshNode(event.targetPath.parent)

        } catch (e: Exception) {
            logger.error("Failed to move file: ${event.sourcePath}", e)
        }
    }
    
    private fun refreshNode(path: Path?) {
        // Placeholder for refresh logic
    }

    /**
     * Loads the children for a given directory, sorting them and compacting empty parent directories.
     */
    private fun loadChildren(directory: Path): List<TreeItem<FileTreeItem>> {
        return try {
            // This set tracks paths that have been processed as part of a compacted directory
            // to avoid adding them as duplicate, separate entries in the tree.
            val processedPaths = mutableSetOf<Path>()
            directory.listDirectoryEntries()
                .sortedWith(compareBy<Path> { !it.isDirectory() }.thenComparator { a, b ->
                    naturalOrderComparator.compare(a.fileName.toString(), b.fileName.toString())
                })
                .mapNotNull { entry ->
                    if (entry in processedPaths) return@mapNotNull null

                    if (entry.isDirectory()) {
                        val (finalPath, displayName) = findCompactedPath(entry)
                        var current = finalPath
                        while (current != entry.parent) {
                            if (current != entry) processedPaths.add(current)
                            current = current.parent ?: break
                        }
                        createNode(FileTreeItem(finalPath, displayName))
                    } else {
                        createNode(FileTreeItem(entry, entry.fileName.toString()))
                    }
                }
        } catch (e: Exception) {
            logger.error("Failed to load children for directory: $directory", e)
            emptyList()
        }
    }

    /**
     * Compacts chains of single-child directories into a single, dot-separated display name.
     * For example, a structure like `src/main/kotlin` where `src` and `main` only contain
     * one directory will be treated as a single logical node with the display name "src.main.kotlin".
     *
     * @param startPath The initial directory to begin compaction from.
     * @return A Pair containing the final, deepest path in the chain and the compacted display name.
     */ //TODO: Add settings to change separation character
    private fun findCompactedPath(startPath: Path): Pair<Path, String> {
        var currentPath = startPath
        val nameParts = mutableListOf(startPath.fileName.toString())

        while (true) {
            val entries = try {
                currentPath.listDirectoryEntries()
            } catch (e: Exception) {
                logger.info("Cannot read directory during compaction: {}", currentPath, e)
                emptyList()
            }
            if (entries.size == 1 && entries.first().isDirectory()) {
                currentPath = entries.first()
                nameParts.add(currentPath.fileName.toString())
            } else {
                break
            }
        }
        return Pair(currentPath, nameParts.joinToString("."))
    }

    private fun createNode(itemData: FileTreeItem): TreeItem<FileTreeItem> {
        val treeItem = TreeItem(itemData)
        
        addExpansionListener(treeItem, itemData.path)

        if (itemData.path.isDirectory()) {
            treeItem.children.add(TreeItem()) // Fake item for expandability

            treeItem.expandedProperty().addListener { _, _, isExpanded ->
                if (isExpanded && treeItem.children.firstOrNull()?.value == null) {
                    scope.launch {
                        val children = loadChildren(itemData.path)
                        Platform.runLater {
                            treeItem.children.setAll(children)
                            restoreExpandedPaths(treeItem) // Restore children state
                        }
                    }
                }
            }
        }
        return treeItem
    }
    
    private fun addExpansionListener(item: TreeItem<FileTreeItem>, path: Path) {
        item.expandedProperty().addListener { _, _, isExpanded ->
            if (isRestoringExpansion) return@addListener
            
            if (isExpanded) {
                expandedPaths.add(path)
            } else {
                expandedPaths.remove(path)
            }
            WorkspaceManager.updateExpandedPaths(expandedPaths)
        }
    }

    //TODO: Move to utils
    private val naturalOrderComparator = Comparator<String> { a, b ->
        var i = 0
        var j = 0

        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]

            if (ca.isDigit() && cb.isDigit()) {
                var na = StringBuilder()
                var nb = StringBuilder()

                while (i < a.length && a[i].isDigit()) {
                    na.append(a[i])
                    i++
                }
                while (j < b.length && b[j].isDigit()) {
                    nb.append(b[j])
                    j++
                }

                val lenDiff = na.length - nb.length
                val diff = if (lenDiff != 0 ) lenDiff else na.toString().compareTo(nb.toString())
                if (diff != 0) return@Comparator diff
            } else {
                val diff = ca.lowercaseChar() - cb.lowercaseChar()
                if (diff != 0) return@Comparator diff
                i++
                j++
            }
        }

        a.length - b.length
    }
}