package io.github.frostzie.datapackide.events

/**
 * UI-related events
 */

enum class UIAction {
    // Window Controls
    MINIMIZE_WINDOW,
    MAXIMIZE_WINDOW,
    RESTORE_WINDOW,
    CLOSE_WINDOW,
    TOGGLE_WINDOW,

    // Sidebar Actions
    //TODO: TOGGLE_FILE_TREE,
    TOGGLE_SEARCH,
    OPEN_DIRECTORY_CHOOSER,

    // Status Bar Updates
    //TODO: UPDATE_CURSOR_POSITION,
    //TODO: UPDATE_FILE_STATUS,
    //TODO: UPDATE_LINE_COUNT,

    // Theme and Appearance
    //TODO: TOGGLE_CUSTOM_THEMES,
    //TODO: SET_CUSTOM_THEME,
    RELOAD_STYLES,
    RESET_STYLES_TO_DEFAULT,

    // App & Popup State
    SHOW_SETTINGS,
    SHOW_ABOUT,
    //TODO: APP_WARN,
    //TODO: APP_CONFIRM
}

/**
 * Event fired when a UI action is requested
 */
data class UIActionEvent(
    val action: UIAction,
    val data: Map<String, Any> = emptyMap()
)

/**
 * Event fired when window state changes
 */
data class WindowStateEvent(
    val isVisible: Boolean,
    val isMaximized: Boolean = false,
    val isMinimized: Boolean = false
)

/**
 * Event fired when cursor position updates (existing functionality)
 */
data class EditorCursorChangedEvent(
    val line: Int,
    val column: Int,
    val filePath: String?
)

/**
 * Event fired when editor content changes (existing functionality)
 */
data class EditorContentChangedEvent(
    val content: String,
    val filePath: String?
)

/**
 * Event fired when editor focus changes (existing functionality)
 */
data class EditorFocusEvent(
    val hasFocus: Boolean,
    val filePath: String?
)

/**
 * Event fired when status information should be updated
 */
data class StatusUpdateEvent(
    val type: StatusType,
    val message: String,
    val data: Map<String, Any> = emptyMap()
)

enum class StatusType {
    CURSOR_POSITION,
    FILE_INFO
}