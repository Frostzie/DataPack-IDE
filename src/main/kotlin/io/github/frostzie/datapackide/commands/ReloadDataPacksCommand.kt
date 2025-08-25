package io.github.frostzie.datapackide.commands

import io.github.frostzie.datapackide.utils.LoggerProvider
import net.minecraft.client.MinecraftClient

object ReloadDataPacksCommand {

    private val logger = LoggerProvider.getLogger("ReloadDataPacksCommand")

    fun executeCommandButton() {
        MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("reload")
        logger.debug("Sent /reload with button press")
    }

    fun executeCommandHotKey() {
        //TODO: Add a hotkey to execute the command
    }
}