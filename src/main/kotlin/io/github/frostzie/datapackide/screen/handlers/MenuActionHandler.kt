package io.github.frostzie.datapackide.screen.handlers

import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.screen.elements.bars.StatusBar
import io.github.frostzie.datapackide.screen.elements.popup.NewFileWindow
import io.github.frostzie.datapackide.screen.elements.popup.Settings
import io.github.frostzie.datapackide.utils.FileUtils
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.Stage

/**
 * Handles all menu actions for the DataPack IDE
 * Separates logic from UI initialization
 */
class MenuActionHandler(
    private val textEditor: TextEditor?,
    private val statusBar: StatusBar?,
    private val parentStage: Stage?
) {

    companion object {
        private val logger = LoggerProvider.getLogger("MenuActionHandler")
    }

    // File Menu Actions
    fun createNewFile() {
        val newFileWindow = NewFileWindow(parentStage)
        val result = newFileWindow.showAndWaitForResult()

        result?.let {
            val fileInfo = FileUtils.createNewFile(it.fileName, it.fileType)

            if (fileInfo != null) {
                val content = FileUtils.loadFile(fileInfo.path)
                if (content != null) {
                    textEditor?.setText(content, fileInfo.path.toString())
                    logger.info("New file created and opened: ${fileInfo.name}")
                } else {
                    logger.error("Could not read the newly created file: ${fileInfo.path}")
                    // TODO: Show an error alert to the user
                }
            } else {
                logger.error("Failed to create new file '${it.fileName}'. It might already exist.")
                // TODO: Show an error alert to the user
            }
        }
    }

    fun openFile() {
        val files = FileUtils.listDatapackFiles()

        if (files.isNotEmpty()) {
            // TODO: Implement proper file choosing window
            val mostRecent = files.first()
            val content = FileUtils.loadFile(mostRecent.path)

            if (content != null) {
                textEditor?.setText(content, mostRecent.path.toString())
                logger.info("File opened: ${mostRecent.name} (${content.length} characters)")
            } else {
                logger.error("Failed to load file: ${mostRecent.name}")
            }
        } else {
            logger.info("No existing files found, creating sample file...")
            createNewFile()
        }
    }

    fun saveCurrentFile() {
        val content = textEditor?.getText() ?: ""
        val currentPath = textEditor?.getCurrentFilePath()

        if (currentPath != null && currentPath != "Untitled") {
            val filePath = java.nio.file.Paths.get(currentPath)
            if (FileUtils.saveFile(filePath, content)) {
                logger.info("File saved: $currentPath (${content.length} characters)")
                textEditor?.markAsSaved()
            } else {
                logger.error("Failed to save file: $currentPath")
            }
        } else {
            saveAsFile()
        }
    }

    fun saveAsFile() {
        val content = textEditor?.getText() ?: ""
        val fileInfo = FileUtils.saveAsNewFile(
            content = content,
            type = FileUtils.FileType.JSON,
            baseName = "datapack_file"
        )

        if (fileInfo != null) {
            textEditor?.setText(content, fileInfo.path.toString())
            logger.info("File saved as: ${fileInfo.name} (${content.length} characters)")
        } else {
            logger.error("Failed to save file as new file")
        }
    }

    fun closeCurrentFile() {
        if (textEditor?.isModified() == true) {
            // TODO: Show confirmation dialog
            logger.info("File has unsaved changes, showing confirmation dialog...")
        }
        textEditor?.newFile()
        logger.info("File closed")
    }

    // Edit Menu Actions
    fun performUndo() {
        textEditor?.undo()
        logger.debug("Undo performed")
    }

    fun performRedo() {
        textEditor?.redo()
        logger.debug("Redo performed")
    }

    fun performCut() {
        textEditor?.cut()
        logger.debug("Cut performed")
    }

    fun performCopy() {
        textEditor?.copy()
        logger.debug("Copy performed")
    }

    fun performPaste() {
        textEditor?.paste()
        logger.debug("Paste performed")
    }

    fun showFindDialog() {
        // TODO: Implement find dialog
        logger.info("Find dialog requested")
    }

    fun showReplaceDialog() {
        // TODO: Implement replace dialog
        logger.info("Replace dialog requested")
    }

    // Datapack Menu Actions
    fun runDatapack() {
        val content = textEditor?.getText() ?: ""
        logger.info("Running datapack with ${content.length} characters")
        // TODO: Implement datapack execution
    }

    fun validateDatapack() {
        val content = textEditor?.getText() ?: ""
        logger.info("Validating datapack with ${content.length} characters")
        // TODO: Implement datapack validation
    }

    fun packageDatapack() {
        logger.info("Packaging datapack")
        // TODO: Implement datapack packaging
    }

    // Help Menu Actions
    fun showPreferences() {
        val settingsWindow = Settings(parentStage)
        settingsWindow.show()
        logger.info("Settings window opened")
    }

    fun showAbout() {
        // TODO: Implement about dialog
        logger.info("About dialog requested")
    }
}