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
    var iconPath: Path? = null,
    val additionalPaths: MutableList<Path> = mutableListOf()
) {
    fun loadMetadata() {
        metadata = DatapackParser.parse(path)

        // It indeed can only have pack.png
        val pack = path.resolve("pack.png")
        if (pack.exists()) {
            iconPath = pack
        }
    }
}
