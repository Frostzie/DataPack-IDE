package io.github.frostzie.datapackide.eventsOLD

@Deprecated("Replacing with newer system")
/**
 * Event fired when a UI action is requested
 */
data class UIActionEvent(
    val data: Map<String, Any> = emptyMap()
)

@Deprecated("Replacing with newer system")
/**
 * Event fired when cursor position updates (existing functionality)
 */
data class EditorCursorChangedEvent(
    val line: Int,
    val column: Int,
    val filePath: String?
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when editor content changes (existing functionality)
 */
data class EditorContentChangedEvent(
    val content: String,
    val filePath: String?
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when editor focus changes (existing functionality)
 */
data class EditorFocusEvent(
    val hasFocus: Boolean,
    val filePath: String?
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when status information should be updated
 */
data class StatusUpdateEvent(
    val message: String,
    val data: Map<String, Any> = emptyMap()
)