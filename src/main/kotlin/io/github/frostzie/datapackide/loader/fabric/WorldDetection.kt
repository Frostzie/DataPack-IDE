package io.github.frostzie.datapackide.loader.fabric

import net.minecraft.client.Minecraft
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path

object WorldDetection {
    /**
     * Check if player connected to a world
     */
    fun isWorldOpen(): Boolean {
        return Minecraft.getInstance().level != null
    }

    /**
     * Check if player connected to singleplayer
     */
    fun isSingleplayer(): Boolean {
        return Minecraft.getInstance().singleplayerServer != null
    }

    /**
     * Check if player connected to server
     */
    fun isServer(): Boolean {
        return Minecraft.getInstance().currentServer != null
    }

    /**
     * Get world path
     */
    fun getWorldPath(): Path? {
        return Minecraft.getInstance().singleplayerServer?.getWorldPath(LevelResource.ROOT)
    }
}