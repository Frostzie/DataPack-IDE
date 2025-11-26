package io.github.frostzie.datapackide.commands

import io.github.frostzie.datapackide.utils.LoggerProvider
import net.minecraft.client.Minecraft

object ReloadDataPacksCommand {

    private val logger = LoggerProvider.getLogger("ReloadDataPacksCommand")

    fun executeCommandButton() {
        Minecraft.getInstance().player?.connection?.sendCommand("reload")
        logger.debug("Sent /reload with button press")
    }

    fun executeCommandHotKey() {
        //TODO: Add a hotkey to execute the command
    }
}