package io.github.frostzie.datapackide.events.handlers

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.screen.elements.popup.NewFileWindow
import io.github.frostzie.datapackide.utils.FileUtils
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.Stage
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Handles file-related actions such as creating, opening, saving, and deleting files.
 */
class FileActionHandler(
    private val textEditor: TextEditor?,
    private val parentStage: Stage?
) {
    companion object {
        private val logger = LoggerProvider.getLogger("FileActionHandler")
    }

    fun initialize() {
        EventBus.register<FileActionEvent> { event ->
            logger.debug("Handling file action: ${event.action}")
            handleFileAction(event)
        }

        EventBus.register<FileOpenEvent> { event ->
            logger.info("FileOpenEvent received: ${event.filePath}")
            openFile(event.filePath)
        }

        EventBus.register<DirectorySelectedEvent> { event ->
            logger.info("Directory selected: ${event.directoryPath}")
            // FileTreeView will handle this event
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
}