package io.github.frostzie.datapackide.utils.file

import net.minecraft.client.MinecraftClient
import net.minecraft.server.integrated.IntegratedServer
import net.minecraft.util.WorldSavePath
import java.nio.file.Path

object DirectoryChooseUtils {

    /**
     * Gets the instance folder path.
     */
    fun getInstancePath(): Path? {
        val client = MinecraftClient.getInstance()

        return client.runDirectory?.toPath()
    }

    /**
     * Gets the datapack folder path for the current world.
     * Returns null if not in singleplayer.
     */
    fun getDatapackPath(): Path? {
        val client = MinecraftClient.getInstance()
        val server: IntegratedServer? = client.server

        return server?.getSavePath(WorldSavePath.DATAPACKS)
    }

    /**
     * Checks if the player is in singleplayer (integrated server).
     */
    fun isSingleplayer(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.isIntegratedServerRunning
    }
}