package io.github.frostzie.datapackide.events.handlers

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.screen.elements.main.FileTreeView
import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.screen.elements.popup.NewFileWindow
import io.github.frostzie.datapackide.utils.FileUtils
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.Stage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * Handles file-related actions such as creating, opening, saving, and deleting files.
 */
class FileActionHandler(
    private val textEditor: TextEditor?,
    private val fileTreeView: FileTreeView?,
    private val parentStage: Stage?
) {
    companion object {
        private val logger = LoggerProvider.getLogger("FileActionHandler")
    }

    fun initialize() {
        EventBus.register<FileActionEvent> { event ->
            logger.debug("Handling file action: {}", event.action)
            handleFileAction(event)
        }

        EventBus.register<FileOpenEvent> { event ->
            logger.info("FileOpenEvent received: ${event.filePath}")
            openFile(event.filePath)
        }

        EventBus.register<DirectorySelectedEvent> { event ->
            logger.info("Directory selected event fired: ${event.directoryPath}")
        }
        logger.info("FileActionHandler initialized")
    }

    private fun handleFileAction(event: FileActionEvent) {
        when (event.action) {
            FileAction.NEW_FILE -> createNewFile()
            FileAction.OPEN_FILE -> openMostRecentFileOrNew()
            FileAction.SAVE_FILE -> saveCurrentFile()
            FileAction.SAVE_AS_FILE -> saveAsFile()
            FileAction.CLOSE_FILE -> closeCurrentFile()
            FileAction.DELETE_FILE -> event.filePath?.let { deleteFile(it) }
            FileAction.RELOAD_FILE -> event.filePath?.let { openFile(it) }
            FileAction.MOVE -> handleFileMove(event)
        }
    }

    private fun openFile(filePath: Path) {
        try {
            val content = filePath.toFile().readText()
            textEditor?.setText(content, filePath.toString())
            logger.info("File opened successfully: ${filePath.fileName}")
        } catch (e: Exception) {
            logger.error("Failed to open file: $filePath", e)
            EventBus.post(FileOperationCompleteEvent(
                FileAction.OPEN_FILE, false, filePath, "Failed to open file", e
            ))
        }
    }

    private fun createNewFile() {
        val newFileWindow = NewFileWindow(parentStage)
        val result = newFileWindow.showAndWaitForResult()

        result?.let {
            val fileInfo = FileUtils.createNewFile(it.fileName, it.fileType)
            if (fileInfo != null) {
                val content = FileUtils.loadFile(fileInfo.path)
                if (content != null) {
                    textEditor?.setText(content, fileInfo.path.toString())
                    EventBus.post(FileOperationCompleteEvent(
                        FileAction.NEW_FILE, true, fileInfo.path, "File created successfully"
                    ))
                }
            }
        }
    }

    private fun openMostRecentFileOrNew() {
        val files = FileUtils.listDatapackFiles()
        if (files.isNotEmpty()) {
            val mostRecent = files.first()
            EventBus.post(FileOpenEvent(mostRecent.path))
        } else {
            createNewFile()
        }
    }

    private fun saveCurrentFile() {
        val content = textEditor?.getText() ?: ""
        val currentPath = textEditor?.getCurrentFilePath()

        if (currentPath != null && currentPath != "Untitled") {
            val filePath = Paths.get(currentPath)
            if (FileUtils.saveFile(filePath, content)) {
                textEditor.markAsSaved()
                EventBus.post(FileSavedEvent(filePath, content))
            }
        } else {
            saveAsFile()
        }
    }

    private fun saveAsFile() {
        val content = textEditor?.getText() ?: ""
        val fileInfo = FileUtils.saveAsNewFile(content, FileUtils.FileType.JSON, "datapack_file")

        fileInfo?.let {
            textEditor?.setText(content, it.path.toString())
            EventBus.post(FileSavedEvent(it.path, content))
        }
    }

    private fun closeCurrentFile() {
        if (textEditor?.isModified() == true) {
            // TODO: Show confirmation dialog
            logger.info("File has unsaved changes")
        }
        textEditor?.newFile()
    }

    private fun deleteFile(filePath: Path) {
        if (FileUtils.deleteFile(filePath)) {
            EventBus.post(FileOperationCompleteEvent(
                FileAction.DELETE_FILE, true, filePath, "File deleted successfully"
            ))
        }
    }

    private fun handleFileMove(event: FileActionEvent) {
        val sourcePath = event.metadata["sourcePath"] as? Path
        val targetPath = event.metadata["targetPath"] as? Path

        if (sourcePath == null || targetPath == null) {
            logger.error("File move action received with missing source or target path in metadata.")
            return
        }

        try {
            if (sourcePath.exists()) {
                Files.move(sourcePath, targetPath)

                logger.info("File moved: ${sourcePath.fileName} -> ${targetPath.parent.fileName}/")

                fileTreeView?.refreshDirectory()

                EventBus.post(FileOperationCompleteEvent(FileAction.MOVE, true, targetPath, "File moved successfully: ${sourcePath.fileName} -> ${targetPath.parent.fileName}/"))
            }
        } catch (e: Exception) {
            logger.error("Failed to move file: ${sourcePath.fileName}", e)
            EventBus.post(FileOperationCompleteEvent(FileAction.MOVE, false, sourcePath, "Failed to move file: ${sourcePath.fileName}", e))
        }
    }
}