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

object DirectoryChooseUtils {
    private val logger = LoggerProvider.getLogger("DirectoryChooseUtils")

    /**
     * Prompts the user to select a project directory and fires DirectorySelected event.
     */
    fun promptOpenProject(ownerWindow: Window?) {
        val directoryChooser = DirectoryChooser().apply {
            title = "Open Project"
            try {
                val datapackPath = getDatapackPath()
                initialDirectory = if (isSingleplayer() && datapackPath != null) {
                    datapackPath.toFile()
                } else {
                    getInstancePath()?.toFile()
                }
            } catch (e: Exception) {
                logger.warn("Could not set initial directory", e)
            }
        }

        val selectedDirectory = directoryChooser.showDialog(ownerWindow)
        if (selectedDirectory != null) {
            EventBus.post(DirectorySelected(selectedDirectory.toPath()))
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