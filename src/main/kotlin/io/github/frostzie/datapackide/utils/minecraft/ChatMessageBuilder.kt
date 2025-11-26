package io.github.frostzie.datapackide.utils.minecraft

import io.github.frostzie.datapackide.utils.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import java.net.URI

object ChatMessageBuilder {
    fun warning(warningMessage: String, copyableText: String = warningMessage) {
        val fullMessage = buildMessage(warningMessage, copyableText, "WARN", 0xFFA500, "warning")

        Minecraft.getInstance()?.execute {
            Minecraft.getInstance().player?.displayClientMessage(fullMessage, false)
        }
    }

    fun error(errorMessage: String, copyableText: String = errorMessage) {
        val fullMessage = buildMessage(errorMessage, copyableText, "ERROR", 0xFF5555, "error")

        Minecraft.getInstance()?.execute {
            Minecraft.getInstance().player?.displayClientMessage(fullMessage, false)
        }
    }

    private fun buildMessage(message: String, copyableText: String, level: String, color: Int, type: String): Component {
        val styledMessage = Component.literal(message)
            .setStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(color))
                    .withClickEvent(ClickEvent.CopyToClipboard(copyableText))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to copy $type message")))
            )
        val builder = Component.empty()
            .append(Component.literal("§7["))
            .append(ColorUtils.dataPackIDEPrefixChat())
            .append(Component.literal("§7]§r "))
            .append(Component.literal("[$level]§r ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))))
            .append(styledMessage)

            if (level == "ERROR") {
            builder.append(
                Component.literal("\nPlease report this in the discord server!")
                    .setStyle(
                        Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))
                            .withClickEvent(ClickEvent.OpenUrl(URI("https://discord.gg/qZ885qTvkx")))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to open the Discord server invite")))
                    )
            )
        }
        return builder
    }
}
