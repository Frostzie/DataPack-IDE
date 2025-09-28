package io.github.frostzie.datapackide.eventsOLD

/**
 * Menu-related events
 */
@Deprecated("Replacing with newer system")
enum class MenuCategory {
    FILE,
    EDIT,
    DATAPACK,
    HELP
}
@Deprecated("Replacing with newer system")
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
}
@Deprecated("Replacing with newer system")
/**
 * Event fired when a menu item is selected
 */
data class MenuActionEvent(
    val category: MenuCategory,
    val action: MenuAction,
    val metadata: Map<String, Any> = emptyMap()
)
@Deprecated("Replacing with newer system")
/**
 * Event fired when menu visibility changes
 */
data class MenuVisibilityEvent(
    val category: MenuCategory? = null, // null = all menus
    val visible: Boolean
)