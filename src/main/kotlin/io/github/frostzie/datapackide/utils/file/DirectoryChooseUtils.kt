package io.github.frostzie.datapackide.utils.file

import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.DirectoryChooser
import javafx.stage.Window
import net.minecraft.client.Minecraft
import net.minecraft.client.server.IntegratedServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path
import io.github.frostzie.datapackide.project.validation.ProjectValidator
import io.github.frostzie.datapackide.project.validation.ValidationResult
import io.github.frostzie.datapackide.modules.project.preview.*
import io.github.frostzie.datapackide.screen.elements.project.preview.ProjectPreviewView
import javafx.scene.control.ButtonBar
import javafx.stage.FileChooser
import javafx.concurrent.Task

object DirectoryChooseUtils {
    private val logger = LoggerProvider.getLogger("DirectoryChooseUtils")

    /**
     * Shows the user to select a project (Folder or Zip), validates it, and fires DirectorySelected.
     */
    fun promptOpenProject(ownerWindow: Window?) {
        val directoryChooser = DirectoryChooser().apply {
            title = "Open Project Folder"
            try {
                val datapackPath = getDatapackPath()
                initialDirectory = if (isSingleplayer() && datapackPath != null) datapackPath.toFile() else getInstancePath()?.toFile()
            } catch (e: Exception) { logger.warn("Could not set initial directory", e) }
        }

        val selectedFile = directoryChooser.showDialog(ownerWindow)
        if (selectedFile != null) {
            handleSelection(selectedFile.toPath())
        }
    }
    
    /**
     * Shows the user to select a project Zip file, validates it, and fires DirectorySelected.
     */
    fun promptOpenZip(ownerWindow: Window?) {
        val fileChooser = FileChooser().apply {
            title = "Open Project Zip"
            extensionFilters.add(FileChooser.ExtensionFilter("Zip Files", "*.zip"))
            try {
                val datapackPath = getDatapackPath()
                initialDirectory = if (isSingleplayer() && datapackPath != null) datapackPath.toFile() else getInstancePath()?.toFile()
            } catch (e: Exception) { logger.warn("Could not set initial directory", e) }
        }

        val selectedFile = fileChooser.showOpenDialog(ownerWindow)
        if (selectedFile != null) {
            handleSelection(selectedFile.toPath())
        }
    }

    /**
     * Call this when a path is selected (from chooser or drag-drop).
     */
    fun handleSelection(path: Path) {
        val result = ProjectValidator.validate(path)
        
        val viewModel = when (result) {
            is ValidationResult.ValidSingle -> SingleProjectPreviewViewModel(result.path, result.metadata)
            is ValidationResult.ValidZip -> ZipProjectPreviewViewModel(result.path, result.metadata)
            is ValidationResult.ValidWorkspace -> WorkspaceProjectPreviewViewModel(result.root, result.projects)
            is ValidationResult.Invalid -> InvalidProjectPreviewViewModel(result.path, result.reason)
        }

        val dialog = ProjectPreviewView(viewModel)
        
        val button = dialog.showAndWait()
        if (button.isPresent && button.get().buttonData == ButtonBar.ButtonData.OK_DONE) {
            val task = object : Task<Path>() {
                override fun call(): Path {
                    return viewModel.onConfirm()
                }
            }

            task.setOnSucceeded { EventBus.post(DirectorySelected(task.value)) }
            
            task.setOnFailed {
                logger.error("Failed to open/import project", task.exception)
            }

            Thread(task).start() //TODO: Add loading indicator
        }
    }

    /**
     * Gets the instance folder path.
     */
    fun getInstancePath(): Path? {
        val client = Minecraft.getInstance()

        return client.gameDirectory?.toPath()
    }

    /**
     * Gets the datapack folder path for the current world.
     * Returns null if not in singleplayer.
     */
    fun getDatapackPath(): Path? {
        val client = Minecraft.getInstance()
        val server: IntegratedServer? = client.singleplayerServer

        return server?.getWorldPath(LevelResource.DATAPACK_DIR)
    }

    /**
     * Checks if the player is in singleplayer (integrated server).
     */
    fun isSingleplayer(): Boolean {
        val client = Minecraft.getInstance()
        return client.hasSingleplayerServer()
    }
}