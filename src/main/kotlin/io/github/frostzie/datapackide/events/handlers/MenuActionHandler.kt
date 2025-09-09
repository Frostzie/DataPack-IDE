package io.github.frostzie.datapackide.events.handlers

import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand
import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.utils.LoggerProvider

/**
 * Handles menu actions and routes them to the appropriate components.
 */
class MenuActionHandler(private val textEditor: TextEditor?) {
    companion object {
        private val logger = LoggerProvider.getLogger("MenuActionHandler")
    }

    fun initialize() {
        EventBus.register<MenuActionEvent> { event ->
            logger.debug("Handling menu action: ${event.category}.${event.action}")

            when (event.category) {
                MenuCategory.FILE -> handleFileMenuAction(event.action)
                MenuCategory.EDIT -> handleEditMenuAction(event.action)
                MenuCategory.DATAPACK -> handleDatapackMenuAction(event.action)
                MenuCategory.HELP -> handleHelpMenuAction(event.action)
            }
        }

        EventBus.register<MenuVisibilityEvent> { event ->
            logger.debug("Menu visibility changed: ${event.category} = ${event.visible}")
        }
        logger.info("MenuActionHandler initialized")
    }

    private fun handleFileMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.NEW_FILE -> EventBus.post(FileActionEvent(FileAction.NEW_FILE))
            MenuAction.OPEN_FILE -> EventBus.post(FileActionEvent(FileAction.OPEN_FILE))
            MenuAction.SAVE_FILE -> EventBus.post(FileActionEvent(FileAction.SAVE_FILE))
            MenuAction.SAVE_AS_FILE -> EventBus.post(FileActionEvent(FileAction.SAVE_AS_FILE))
            MenuAction.CLOSE_FILE -> EventBus.post(FileActionEvent(FileAction.CLOSE_FILE))
            MenuAction.EXIT -> EventBus.post(UIActionEvent(UIAction.REQUEST_WINDOW_CLOSE))
            else -> logger.warn("Unhandled file menu action: $action")
        }
    }

    private fun handleEditMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.UNDO -> textEditor?.undo()
            MenuAction.REDO -> textEditor?.redo()
            MenuAction.CUT -> textEditor?.cut()
            MenuAction.COPY -> textEditor?.copy()
            MenuAction.PASTE -> textEditor?.paste()
            MenuAction.SELECT_ALL -> textEditor?.selectAll()
            MenuAction.FIND -> logger.info("Find dialog requested") // TODO: Post UIActionEvent
            MenuAction.REPLACE -> logger.info("Replace dialog requested") // TODO: Post UIActionEvent
            else -> logger.warn("Unhandled edit menu action: $action")
        }
    }

    private fun handleDatapackMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.RELOAD_DATAPACKS -> ReloadDataPacksCommand.executeCommandButton()
            MenuAction.VALIDATE_DATAPACK -> logger.info("Validate datapack requested") // TODO: Implement
            MenuAction.PACKAGE_DATAPACK -> logger.info("Package datapack requested") // TODO: Implement
            else -> logger.warn("Unhandled datapack menu action: $action")
        }
    }

    private fun handleHelpMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.PREFERENCES -> EventBus.post(UIActionEvent(UIAction.SHOW_SETTINGS))
            MenuAction.ABOUT -> EventBus.post(UIActionEvent(UIAction.SHOW_ABOUT))
            else -> logger.warn("Unhandled help menu action: $action")
        }
    }
}