package io.github.frostzie.datapackide.project.state

import java.nio.file.Path

/**
 * Represents the UI state for a specific project workspace.
 */
data class ProjectState(
    val openFiles: Set<Path> = emptySet(),
    val activeFile: Path? = null,
    val expandedPaths: Set<Path> = emptySet()
)
