package io.github.frostzie.datapackide.utils

import javafx.scene.effect.ColorAdjust
import javafx.scene.paint.Color
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor

/**
 * Utility class for color manipulation and conversion to ColorAdjust parameters
 */
object ColorUtils {
    private val logger = LoggerProvider.getLogger("ColorUtils")

    /**
     * Creates a ColorAdjust effect to transform a WHITE icon to a target color.
     * This logic is based on the standard method for colorizing white sources,
     * which requires inverting brightness and offsetting hue.
     *
     * @param hexColor Color in hex format (e.g., "#f0eded", "#FF0000")
     * @return ColorAdjust effect that will transform white pixels to the target color
     */
    fun createColorAdjustForWhiteIcon(hexColor: String): ColorAdjust {
        val color = parseHexColor(hexColor)

        val hueAdjust = (((color.hue + 180) % 360) / 180.0) - 1.0
        val saturationAdjust = color.saturation

        val brightnessAdjust = color.brightness - 1.0

        return ColorAdjust().apply {
            this.hue = hueAdjust
            this.saturation = saturationAdjust
            this.brightness = brightnessAdjust
            contrast = 0.0
        }
    }

    /**
     * Parses hex color string to JavaFX Color
     */
    private fun parseHexColor(hexColor: String): Color {
        return try {
            val cleanHex = hexColor.removePrefix("#")
            when (cleanHex.length) {
                3 -> {
                    val r = cleanHex[0].toString().repeat(2)
                    val g = cleanHex[1].toString().repeat(2)
                    val b = cleanHex[2].toString().repeat(2)
                    Color.web("#$r$g$b")
                }
                6 -> Color.web("#$cleanHex")
                8 -> Color.web("#$cleanHex")
                else -> {
                    logger.warn("Invalid hex color format: $hexColor, using default")
                    Color.WHITE
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse hex color: $hexColor", e)
            Color.WHITE
        }
    }

    fun dataPackIDEPrefixChat(): MutableText {
        val text = "DataPack IDE"
        val colors = listOf(
            0x14EE72, // D
            0x19E77F, // a
            0x1FE08B, // t
            0x24D998, // a
            0x29D1A4, // P
            0x2FCAB1, // a
            0x34C3BD, // c
            0x2AB9CA, // k
            0x000000, //
            0x15A4E5, // I
            0x0A9AF2, // D
            0x0090FF, // E
        )
        val result = Text.empty()
        for (i in text.indices) {
            result.append(
                Text.literal(text[i].toString())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colors[i])))
            )
        }
        return result
    }
}