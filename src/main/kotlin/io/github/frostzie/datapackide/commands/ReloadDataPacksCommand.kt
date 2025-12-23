package io.github.frostzie.datapackide.commands

import io.github.frostzie.datapackide.styling.common.IconSource
import io.github.frostzie.datapackide.styling.messages.MessageFactory
import io.github.frostzie.datapackide.styling.messages.MessageSeverity
import io.github.frostzie.datapackide.styling.messages.NotificationPosition
import io.github.frostzie.datapackide.utils.LoggerProvider
import net.minecraft.client.Minecraft
import org.kordamp.ikonli.material2.Material2OutlinedMZ

object ReloadDataPacksCommand {

    private val logger = LoggerProvider.getLogger("ReloadDataPacksCommand")

    /**
     * Executes the `/reload` command in Minecraft and shows a success notification.
     * This is typically triggered by a UI button press.
     */
    fun executeCommandButton() {
        Minecraft.getInstance().player?.connection?.sendCommand("reload")
        logger.debug("Sent /reload with button press")
        val message = MessageFactory.createAndShow(
            title = "Reloaded",
            description = "Datapack Reloaded!",
            severity = MessageSeverity.SUCCESS,
            position = NotificationPosition.BOTTOM_RIGHT,
            icon = IconSource.IkonIcon(Material2OutlinedMZ.REFRESH),
            maxMessages = 1,
            durationMillis = 2500
        )
        message
    }

    /**
     * Placeholder for executing the `/reload` command via a hotkey.
     */
    fun executeCommandHotKey() {
        //TODO: Add a hotkey to execute the command
    }
}