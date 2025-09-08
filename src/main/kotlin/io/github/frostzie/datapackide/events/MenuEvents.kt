package io.github.frostzie.datapackide.events

/**
 * Menu-related events
 */

enum class MenuCategory {
    FILE,
    EDIT,
    DATAPACK,
    HELP
}

enum class MenuAction {
    // File Menu
    NEW_FILE,
    OPEN_FILE,
    SAVE_FILE,
    SAVE_AS_FILE,
    CLOSE_FILE,
    EXIT,

    // Edit Menu
    UNDO,
    REDO,
    CUT,
    COPY,
    PASTE,
    FIND,
    REPLACE,
    SELECT_ALL,

    // Datapack Menu
    VALIDATE_DATAPACK,
    PACKAGE_DATAPACK,
    RELOAD_DATAPACKS,

    // Help Menu
    PREFERENCES,
    ABOUT,
    //TODO: DOCUMENTATION
}

/**
 * Event fired when a menu item is selected
 */
data class MenuActionEvent(
    val category: MenuCategory,
    val action: MenuAction,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Event fired when menu visibility changes
 */
data class MenuVisibilityEvent(
    val category: MenuCategory? = null, // null = all menus
    val visible: Boolean
)