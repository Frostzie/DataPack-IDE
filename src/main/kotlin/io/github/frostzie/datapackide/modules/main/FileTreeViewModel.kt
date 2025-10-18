package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MoveFile
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
import java.nio.file.StandardCopyOption
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class FileTreeViewModel {
    private val logger = LoggerProvider.getLogger("FileTreeViewModel")
    val root = SimpleObjectProperty<TreeItem<FileTreeItem>>()
    var rootDirectory: Path? = null
    private val fileWatcherLock = Any()
    private var fileWatcher: FileSystemWatcher? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        EventBus.register(this)
        logger.info("FileTreeViewModel initialized and registered with EventBus.")
    }

    fun setWindowFocused(focused: Boolean) {
        // Acquire the same lock used when replacing the watcher to avoid races.
        synchronized(fileWatcherLock) {
            fileWatcher?.setWindowFocused(focused)
        }
    }

    fun cleanup() {
        scope.cancel() // Cancel all ongoing operations
        synchronized(fileWatcherLock) {
            fileWatcher?.stop()
            fileWatcher = null
        }
        EventBus.unregister(this)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onDirectorySelected(event: DirectorySelected) {
        val previousRoot = rootDirectory
        rootDirectory = event.directoryPath
        logger.info("Directory selected: ${event.directoryPath}")

        if (previousRoot != event.directoryPath) {
            // Make stop/create/start atomic so setWindowFocused or cleanup cannot observe an inconsistent state.
            synchronized(fileWatcherLock) {
                fileWatcher?.stop()
                fileWatcher = FileSystemWatcher(event.directoryPath)
                fileWatcher?.start()
            }
        }

        scope.launch {
            val currentRoot = root.get()
            val expandedPaths = if (currentRoot != null && previousRoot == event.directoryPath) {
                collectExpandedPaths(currentRoot)
            } else {
                emptySet()
            }

            Platform.runLater {
                val rootNode = TreeItem(FileTreeItem(event.directoryPath, event.directoryPath.fileName.toString()))
                rootNode.isExpanded = true

                val children = loadChildren(event.directoryPath)
                rootNode.children.addAll(children)

                root.set(rootNode)

                // Restore expanded state
                if (expandedPaths.isNotEmpty()) {
                    restoreExpandedPaths(rootNode, expandedPaths)
                }
            }
        }
    }

    private fun collectExpandedPaths(node: TreeItem<FileTreeItem>): Set<Path> {
        val expandedPaths = mutableSetOf<Path>()

        fun traverse(item: TreeItem<FileTreeItem>) {
            if (item.isExpanded && item.value != null) {
                expandedPaths.add(item.value.path)
            }
            item.children.forEach { traverse(it) }
        }

        traverse(node)
        return expandedPaths
    }

    private fun restoreExpandedPaths(node: TreeItem<FileTreeItem>, expandedPaths: Set<Path>) {
        fun traverse(item: TreeItem<FileTreeItem>) {
            if (item.value != null && item.value.path in expandedPaths) {
                item.isExpanded = true
            }
            item.children.forEach { traverse(it) }
        }

        traverse(node)
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

            rootDirectory?.let {
                onDirectorySelected(DirectorySelected(it))
            }
        } catch (e: Exception) {
            logger.error("Failed to move file: ${event.sourcePath}", e)
            // TODO: Show an error message to the user in the UI
        }
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
                .sortedWith(compareBy<Path>({ !it.isDirectory() }).thenComparator { a, b ->
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
     */
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

        if (itemData.path.isDirectory()) {
            treeItem.children.add(TreeItem()) // Fake item for expandability

            treeItem.expandedProperty().addListener { _, _, isExpanded ->
                if (isExpanded && treeItem.children.firstOrNull()?.value == null) {
                    scope.launch {
                        val children = loadChildren(itemData.path)
                        Platform.runLater {
                            treeItem.children.setAll(children)
                        }
                    }
                }
            }
        }
        return treeItem
    }

    // TODO: implement better one later on. Useful info:
    // https://stackoverflow.com/questions/1262239/natural-sort-order-string-comparison-in-java-is-one-built-in
    // https://stackoverflow.com/questions/104599/sort-on-a-string-that-may-contain-a-number
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