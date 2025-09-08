package io.github.frostzie.datapackide.events

import io.github.frostzie.datapackide.events.handlers.FileActionHandler
import io.github.frostzie.datapackide.events.handlers.MenuActionHandler
import io.github.frostzie.datapackide.events.handlers.StatusActionHandler
import io.github.frostzie.datapackide.events.handlers.UIActionHandler
import io.github.frostzie.datapackide.screen.elements.bars.StatusBar
import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.Stage

/**
 * Central event handler system
 * Registers all event listeners and handles application-wide event processing
 */
class EventHandlerSystem(
    private val textEditor: TextEditor?,
    private val statusBar: StatusBar?,
    private val parentStage: Stage?
) {

    companion object {
        private val logger = LoggerProvider.getLogger("EventHandlerSystem")
    }

    private var isInitialized = false

    /**
     * Initialize all event handlers
     */
    fun initialize() {
        if (isInitialized) {
            logger.warn("Event handler system is already initialized. Skipping.")
            return
        }

        MenuActionHandler(textEditor).initialize()
        FileActionHandler(textEditor, parentStage).initialize()
        UIActionHandler(parentStage).initialize()
        StatusActionHandler(statusBar).initialize()

        logger.info("Event handler system initialized")
        isInitialized = true
    }
}