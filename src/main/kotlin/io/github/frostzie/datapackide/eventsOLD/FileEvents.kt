package io.github.frostzie.datapackide.eventsOLD

import io.github.frostzie.datapackide.screen.elements.main.FileTreeNode
import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.Path

@Deprecated("Replacing with newer system")
/**
 * File-related events
 */

enum class FileAction {
    NEW_FILE,
    OPEN_FILE,
    SAVE_FILE,
    SAVE_AS_FILE,
    CLOSE_FILE,
    DELETE_FILE,
    RELOAD_FILE,
    MOVE
}
@Deprecated("Replacing with newer system")
/**
 * Event fired when a file action is requested
 */
data class FileActionEvent(
    val action: FileAction,
    val filePath: Path? = null,
    val content: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when a file should be opened (existing functionality)
 */
data class FileOpenEvent(val filePath: Path)
@Deprecated("Replacing with newer system")
/**
 * Event fired when a directory is selected for file tree (existing functionality)
 */
data class DirectorySelectedEvent(val directoryPath: Path)
@Deprecated("Replacing with newer system")
/**
 * Event fired when a file is saved (existing functionality)
 */
data class FileSavedEvent(
    val filePath: Path,
    val content: String
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when file operations complete
 */
data class FileOperationCompleteEvent(
    val action: FileAction,
    val success: Boolean,
    val filePath: Path? = null,
    val message: String? = null,
    val error: Throwable? = null
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when a node in the FileTreeView requests to be selected
 */
data class NodeSelectionRequestEvent(val node: FileTreeNode)
@Deprecated("Replacing with newer system")
/**
 * Event fired when a drag operation starts in the file tree.
 */
data class FileTreeDragStartEvent(val sourceNode: FileTreeNode)
@Deprecated("Replacing with newer system")
/**
 * Event fired when a drag operation ends in the file tree.
 */
class FileTreeDragEndEvent