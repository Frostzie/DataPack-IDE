package io.github.frostzie.datapackide.project

/**
 * Represents the current workspace configuration, holding a list of open projects.
 */
data class Workspace(
    val projects: MutableList<Project> = mutableListOf()
)
