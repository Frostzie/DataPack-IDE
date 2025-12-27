package io.github.frostzie.datapackide.project.state

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.project.PathTypeAdapter
import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.exists

object ProjectStateHandler {
    private val logger = LoggerProvider.getLogger("ProjectStateHandler")
    private val statesDir = ConfigManager.configDir.resolve("project_states")
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeHierarchyAdapter(Path::class.java, PathTypeAdapter())
        .create()

    init {
        if (!statesDir.exists()) {
            Files.createDirectories(statesDir)
        }
    }

    /**
     * Loads the UI state for a specific project/workspace root.
     */
    fun loadState(rootPath: Path): ProjectState {
        val file = getStateFile(rootPath)
        if (!file.exists()) return ProjectState()

        return try {
            Files.newBufferedReader(file).use { reader ->
                gson.fromJson(reader, ProjectState::class.java) ?: ProjectState()
            }
        } catch (e: Exception) {
            logger.error("Failed to load project state for $rootPath", e)
            ProjectState()
        }
    }

    /**
     * Saves the UI state for a specific project/workspace root.
     */
    fun saveState(rootPath: Path, state: ProjectState) {
        val file = getStateFile(rootPath)
        try {
            Files.newBufferedWriter(file).use { writer ->
                gson.toJson(state, writer)
            }
        } catch (e: Exception) {
            logger.error("Failed to save project state for $rootPath", e)
        }
    }
    
    /**
     * Generates a unique filename based on the path hash.
     * Use MD5 or SHA-1 to get a safe filename.
     */
    private fun getStateFile(path: Path): Path {
        val hash = hashString(path.toAbsolutePath().toString())
        return statesDir.resolve("$hash.json")
    }

    private fun hashString(input: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
