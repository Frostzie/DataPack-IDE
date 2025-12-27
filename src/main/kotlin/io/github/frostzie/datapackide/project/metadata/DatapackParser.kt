package io.github.frostzie.datapackide.project.metadata

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

//TODO: Add format, features, filter, overlays, language
data class DatapackMetadata(
    val description: String,
    val author: String? = null,
)

object DatapackParser {
    private val logger = LoggerProvider.getLogger("DatapackParser")
    private val COLOR_CODE_REGEX = Regex("(?i)§[0-9a-fk-or]")

    fun parse(path: Path): DatapackMetadata? {
        val mcmetaPath = path.resolve("pack.mcmeta")
        if (!mcmetaPath.exists()) return null

        return try {
            val content = mcmetaPath.readText()
            val json = JsonParser.parseString(content).asJsonObject
            val pack = json.getAsJsonObject("pack") ?: return null
            
            var rawDescription = parseDescription(pack.get("description"))
            rawDescription = rawDescription.replace(COLOR_CODE_REGEX, "")
            
            // Split author //TODO: Not even sure if we wanna keep this or nah since not everyone does this (Also finicky way of detecting)
            val (finalDescription, author) = if (rawDescription.contains("\n")) {
                val parts = rawDescription.split("\n", limit = 2)
                Pair(parts[0].trim(), parts[1].trim())
            } else {
                Pair(rawDescription.trim(), null)
            }

            DatapackMetadata( finalDescription, author)
        } catch (e: Exception) {
            logger.error("Failed to parse pack.mcmeta at $path", e)
            null
        }
    }

    private fun parseDescription(element: JsonElement?): String {
        if (element == null) return ""
        if (element.isJsonPrimitive) return element.asString

        // Handle JSON text component (list or object)
        return try {
             if (element.isJsonArray) {
                 element.asJsonArray.joinToString("") { parseDescription(it) }
             } else if (element.isJsonObject) {
                 val obj = element.asJsonObject
                 val text = obj.get("text")?.asString ?: ""
                 val extra = obj.get("extra")
                 val extraText = parseDescription(extra)
                 text + extraText
             } else {
                 element.toString()
             }
        } catch (e: Exception) {
            element.toString()
        }
    }
}