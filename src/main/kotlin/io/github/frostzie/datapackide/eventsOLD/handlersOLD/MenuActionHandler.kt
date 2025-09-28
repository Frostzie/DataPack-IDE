package io.github.frostzie.datapackide.eventsOLD.handlersOLD

import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand
import io.github.frostzie.datapackide.events.AboutMod
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowClose
import io.github.frostzie.datapackide.events.SettingsWindowOpen
import io.github.frostzie.datapackide.eventsOLD.*
import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider

@Deprecated("Replacing with newer system")
/**
 * Handles menu actions and routes them to the appropriate components.
 */
class MenuActionHandler(textEditor: TextEditor?) {
    companion object {
        private val logger = LoggerProvider.getLogger("MenuActionHandler")
    }

    fun initialize() {
        EventBus.register(this)
        logger.info("MenuActionHandler initialized")
    }

    @SubscribeEvent
    fun onMenuAction(event: MenuActionEvent) {
        logger.debug("Handling menu action: {}.{}", event.category, event.action)

        when (event.category) {
            MenuCategory.FILE -> handleFileMenuAction(event.action)
            MenuCategory.DATAPACK -> handleDatapackMenuAction(event.action)
            MenuCategory.HELP -> TODO("Removal")
            MenuCategory.EDIT -> TODO("Removal")
        }
    }

    @SubscribeEvent
    fun onMenuVisibilityChanged(event: MenuVisibilityEvent) {
        logger.debug("Menu visibility changed: {} = {}", event.category, event.visible)
    }

    private fun handleFileMenuAction(action: MenuAction) {
        when (action) {
            MenuAction.NEW_FILE -> EventBus.post(FileActionEvent(FileAction.NEW_FILE))
            MenuAction.OPEN_FILE -> EventBus.post(FileActionEvent(FileAction.OPEN_FILE))
            MenuAction.SAVE_FILE -> EventBus.post(FileActionEvent(FileAction.SAVE_FILE))
            MenuAction.SAVE_AS_FILE -> EventBus.post(FileActionEvent(FileAction.SAVE_AS_FILE))
            MenuAction.CLOSE_FILE -> EventBus.post(FileActionEvent(FileAction.CLOSE_FILE))
            MenuAction.EXIT -> EventBus.post(MainWindowClose()) //TEMP
            else -> logger.warn("Unhandled file menu action: $action")
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
}