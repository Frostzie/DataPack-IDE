package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MoveFile
import io.github.frostzie.datapackide.screen.elements.main.FileTreeItem
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
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

    init {
        EventBus.register(this)
        logger.info("FileTreeViewModel initialized and registered with EventBus.")
    }

    @SubscribeEvent
    fun onDirectorySelected(event: DirectorySelected) {
        rootDirectory = event.directoryPath
        logger.info("Directory selected: ${event.directoryPath}")
        Thread {
            val children = loadChildren(event.directoryPath)
            Platform.runLater {
                val invisibleRoot = TreeItem<FileTreeItem>()
                invisibleRoot.isExpanded = true
                invisibleRoot.children.addAll(children)
                root.set(invisibleRoot)
            }
        }.start()
    }

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
                // Reload the entire tree to reflect the changes.
                // This is simpler and more reliable than trying to manipulate the tree nodes directly.
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
                // Sorts entries to show directories first, then files, both alphabetically. //TODO: Change from Alphabetical to natural order
                .sortedWith(compareBy({ !it.isDirectory() }, { it.fileName.toString().lowercase() }))
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
                    Thread {
                        val children = loadChildren(itemData.path)
                        Platform.runLater {
                            treeItem.children.setAll(children)
                        }
                    }.start()
                }
            }
        }
        return treeItem
    }
}