package io.github.frostzie.datapackide.utils.file

import net.minecraft.client.Minecraft
import net.minecraft.client.server.IntegratedServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path

object DirectoryChooseUtils {

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