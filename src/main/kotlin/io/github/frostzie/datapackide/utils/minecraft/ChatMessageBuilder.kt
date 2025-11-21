package io.github.frostzie.datapackide.utils.minecraft

import io.github.frostzie.datapackide.utils.ColorUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.net.URI

object ChatMessageBuilder {
    fun warning(warningMessage: String, copyableText: String = warningMessage) {
        val fullMessage = buildMessage(warningMessage, copyableText, "WARN", 0xFFA500, "warning")

        MinecraftClient.getInstance()?.execute {
            MinecraftClient.getInstance().player?.sendMessage(fullMessage, false)
        }
    }

    fun error(errorMessage: String, copyableText: String = errorMessage) {
        val fullMessage = buildMessage(errorMessage, copyableText, "ERROR", 0xFF5555, "error")

        MinecraftClient.getInstance()?.execute {
            MinecraftClient.getInstance().player?.sendMessage(fullMessage, false)
        }
    }

    private fun buildMessage(message: String, copyableText: String, level: String, color: Int, type: String): Text {
        val styledMessage = Text.literal(message)
            .setStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(color))
                    .withClickEvent(ClickEvent.CopyToClipboard(copyableText))
                    .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to copy $type message")))
            )
        val builder = Text.empty()
            .append(Text.literal("§7["))
            .append(ColorUtils.dataPackIDEPrefixChat())
            .append(Text.literal("§7]§r "))
            .append(Text.literal("[$level]§r ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))))
            .append(styledMessage)

            if (level == "ERROR") {
            builder.append(
                Text.literal("\nPlease report this in the discord server!")
                    .setStyle(
                        Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))
                            .withClickEvent(ClickEvent.OpenUrl(URI("https://discord.gg/qZ885qTvkx")))
                            .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to open the Discord server invite")))
                    )
            )
        }
        return builder
    }
}
