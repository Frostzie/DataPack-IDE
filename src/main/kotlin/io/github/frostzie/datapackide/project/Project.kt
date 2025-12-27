package io.github.frostzie.datapackide.project

import io.github.frostzie.datapackide.project.metadata.DatapackMetadata
import io.github.frostzie.datapackide.project.metadata.DatapackParser
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Represents a single project (datapack) root in the workspace.
 */
data class Project(
    val path: Path,
    val name: String = path.fileName.toString(),
    var metadata: DatapackMetadata? = null,
    var iconPath: Path? = null
) {
    fun loadMetadata() {
        metadata = DatapackParser.parse(path)

        // No idea if any name.png is supported here so went with the most common ig
        val icon = path.resolve("icon.png")
        val pack = path.resolve("pack.png")
        if (icon.exists()) {
            iconPath = icon
        } else if (pack.exists()) {
            iconPath = pack
        }
    }
}
