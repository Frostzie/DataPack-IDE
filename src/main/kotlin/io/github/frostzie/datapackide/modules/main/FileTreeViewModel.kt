package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.screen.elements.main.FileTreeItem
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class FileTreeViewModel {
    private val logger = LoggerProvider.getLogger("FileTreeViewModel")
    val root = SimpleObjectProperty<TreeItem<FileTreeItem>>()

    init {
        EventBus.register(this)
        logger.info("FileTreeViewModel initialized and registered with EventBus.")
    }

    @SubscribeEvent
    fun onDirectorySelected(event: DirectorySelected) {
        logger.info("Directory selected: ${event.directoryPath}")
        val invisibleRoot = TreeItem<FileTreeItem>()
        invisibleRoot.isExpanded = true
        invisibleRoot.children.setAll(loadChildren(event.directoryPath))
        root.set(invisibleRoot)
    }

    private fun loadChildren(directory: Path): List<TreeItem<FileTreeItem>> {
        return try {
            val processedPaths = mutableSetOf<Path>()
            directory.listDirectoryEntries()
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

    private fun findCompactedPath(startPath: Path): Pair<Path, String> {
        var currentPath = startPath
        val nameParts = mutableListOf(startPath.fileName.toString())

        while (true) {
            val entries = try { currentPath.listDirectoryEntries() } catch (e: Exception) { emptyList() }
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
                    treeItem.children.setAll(loadChildren(itemData.path))
                }
            }
        }
        return treeItem
    }
}