package io.github.frostzie.datapackide.project

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.OpenProjectManagerEvent
import io.github.frostzie.datapackide.events.ResetWorkspaceEvent
import io.github.frostzie.datapackide.events.WorkspaceUpdated
import io.github.frostzie.datapackide.project.metadata.DatapackParser
import io.github.frostzie.datapackide.project.state.ProjectState
import io.github.frostzie.datapackide.project.state.ProjectStateHandler
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.file.FileSystemWatcher
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.LinkedList
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

/**
 * Represents the persisted state of the application session (Global List).
 */
data class SessionState(
    var activeWorkspace: Workspace = Workspace(),
    var recentProjects: LinkedList<Project> = LinkedList()
)

/**
 * Manages the active workspace and persists its state (open projects and history).
 */
object WorkspaceManager {
    private val logger = LoggerProvider.getLogger("WorkspaceManager")
    private val workspaceFile = ConfigManager.configDir.resolve("workspace.json")
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeHierarchyAdapter(Path::class.java, PathTypeAdapter())
        .registerTypeAdapter(Project::class.java, ProjectSerializer())
        .create()

    // The entire session state (workspace + history)
    private var sessionState = SessionState()

    // Shared state for dirty files (unsaved changes)
    val dirtyFiles: ObservableSet<Path> = FXCollections.observableSet()
    
    // File System Watchers
    private val watchers = mutableMapOf<Path, FileSystemWatcher>()
    
    // Public accessors
    val workspace: Workspace
        get() = sessionState.activeWorkspace

    val recentProjects: List<Project>
        get() = sessionState.recentProjects.toList()
        
    val currentWorkspaceRoot: Path?
        get() = workspace.projects.firstOrNull()?.path

    fun initialize() {
        EventBus.register(this)
        load()
    }

    fun load() {
        if (Files.exists(workspaceFile)) {
            try {
                FileReader(workspaceFile.toFile()).use { reader ->
                    val loadedState = gson.fromJson(reader, SessionState::class.java)
                    if (loadedState != null) {
                        val validActiveProjects = loadedState.activeWorkspace.projects
                            .filter { Files.exists(it.path) }
                            .toMutableList()
                        
                        validActiveProjects.forEach {
                            it.loadMetadata()
                            setupWatcher(it.path)
                        }
                            
                        val validRecentProjects = loadedState.recentProjects
                            .filter { Files.exists(it.path) }
                            
                        validRecentProjects.forEach { it.loadMetadata() }

                        sessionState = SessionState(
                            Workspace(validActiveProjects), 
                            LinkedList(validRecentProjects)
                        )
                        loadCurrentState()
                    }
                    logger.debug("Loaded workspace session from {}", workspaceFile)
                }
            } catch (e: Exception) {
                logger.error("Failed to load workspace file, using empty session.", e)
                sessionState = SessionState()
            }
        } else {
            logger.info("Workspace file not found, creating new empty session.")
            sessionState = SessionState()
        }
        EventBus.post(WorkspaceUpdated(workspace))
    }

    fun save() {
        try {
            FileWriter(workspaceFile.toFile()).use { writer ->
                gson.toJson(sessionState, writer)
                logger.debug("Saved workspace session to {}", workspaceFile)
            }
        } catch (e: Exception) {
            logger.error("Failed to save workspace file.", e)
        }
    }

    fun addProject(path: Path) {
        if (workspace.projects.none { it.path == path }) {
            val project = Project(path)
            project.loadMetadata()
            workspace.projects.add(project)
            addToRecent(project)
            loadCurrentState()
            setupWatcher(path) // Start watching
            save()
            EventBus.post(WorkspaceUpdated(workspace))
            logger.debug("Added project to workspace: {}", path)
        } else {
            logger.debug("Project already exists in workspace: {}", path)
        }
    }
    
    fun openSingleProject(path: Path) {
        stopWatchers() // Stop previous
        workspace.projects.clear()
        
        if (DatapackParser.parse(path) != null) {
            logger.debug("Detected single datapack at {}", path)
            addProject(path) // This calls setupWatcher
            return
        }
        
        var foundDatapacks = false
        try {
            val children = path.listDirectoryEntries()
            for (child in children) {
                if (child.isDirectory() && DatapackParser.parse(child) != null) {
                    val project = Project(child)
                    project.loadMetadata()
                    workspace.projects.add(project)
                    setupWatcher(child) // Start watching subproject
                    foundDatapacks = true
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to scan directory for datapacks: $path", e)
        }
        
        if (foundDatapacks) {
             loadCurrentState()
             save()
             EventBus.post(WorkspaceUpdated(workspace))
        } else {
             logger.info("No datapacks detected, opening as generic project: $path")
             addProject(path)
        }
    }

    private fun addToRecent(project: Project) {
        sessionState.recentProjects.removeIf { it.path == project.path }
        sessionState.recentProjects.addFirst(project)
        while (sessionState.recentProjects.size > 20) {
            sessionState.recentProjects.removeLast()
        }
    }

    // State Persistence
    private var currentProjectState = ProjectState()

    fun updateOpenFiles(files: Set<Path>, activeFile: Path?) {
        currentProjectState = currentProjectState.copy(openFiles = files, activeFile = activeFile)
        saveCurrentState()
    }
    
    fun updateExpandedPaths(paths: Set<Path>) {
        currentProjectState = currentProjectState.copy(expandedPaths = paths)
        saveCurrentState()
    }

    private fun saveCurrentState() {
        val root = currentWorkspaceRoot ?: return
        ProjectStateHandler.saveState(root, currentProjectState)
    }
    
    private fun loadCurrentState() {
         val root = currentWorkspaceRoot
        currentProjectState = if (root != null) {
            ProjectStateHandler.loadState(root)
        } else {
            ProjectState()
        }
    }
    
    fun getCurrentState(): ProjectState {
        return currentProjectState
    }
    
    private fun setupWatcher(path: Path) {
        if (watchers.containsKey(path)) return
        val watcher = FileSystemWatcher(path)
        watcher.start()
        watchers[path] = watcher
        logger.debug("Started watcher for: {}", path)
    }
    
    private fun stopWatchers() {
        watchers.values.forEach { it.stop() }
        watchers.clear()
        logger.debug("Stopped all file system watchers")
    }
    
    fun setWindowFocused(focused: Boolean) {
        watchers.values.forEach { it.setWindowFocused(focused) }
    }
    
    fun ignoreWatcherPath(path: Path) {
        watchers.values.forEach { watcher ->
             watcher.ignoreChanges(path)
        }
    }

    // Reset / Navigation
    @SubscribeEvent @Suppress("unused")
    fun onOpenProjectManager(event: OpenProjectManagerEvent) {
        logger.info("Returning to project manager...")
        stopWatchers()
        workspace.projects.clear()
        save()
        EventBus.post(WorkspaceUpdated(workspace))
    }

    @SubscribeEvent @Suppress("unused")
    fun onResetWorkspace(event: ResetWorkspaceEvent) {
        stopWatchers()
        try {
            Files.deleteIfExists(workspaceFile)
        } catch (e: Exception) {
            logger.error("Failed to delete workspace file", e)
        }
        sessionState = SessionState()
        loadCurrentState()
        EventBus.post(WorkspaceUpdated(workspace))
    }
}

/**
 * GSON Adapter for Path serialization
 */
class PathTypeAdapter : TypeAdapter<Path>() {
    override fun write(out: JsonWriter, value: Path?) {
        out.value(value?.toString())
    }

    override fun read(reader: JsonReader): Path? {
        val pathString = reader.nextString()
        return if (pathString != null) Paths.get(pathString) else null
    }
}

/**
 * GSON Adapter for Project serialization (Path only)
 */
class ProjectSerializer : TypeAdapter<Project>() {
    override fun write(out: JsonWriter, value: Project?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("path").value(value.path.toString())
        out.endObject()
    }

    override fun read(reader: JsonReader): Project? {
        var path: Path? = null
        
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "path" -> path = Paths.get(reader.nextString())
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        
        return if (path != null) Project(path) else null
    }
}
