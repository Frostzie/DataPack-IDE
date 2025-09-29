package io.github.frostzie.datapackide.eventsOLD

import io.github.frostzie.datapackide.eventsOLD.handlersOLD.StatusActionHandler
import io.github.frostzie.datapackide.eventsOLD.handlersOLD.UIActionHandler
import io.github.frostzie.datapackide.screen.elements.main.FileTreeView
import io.github.frostzie.datapackide.screen.elements.bars.BottomBarView
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.Stage

@Deprecated("Replacing with newer system")
/**
 * Central event handler system
 * Registers all event listeners and handles application-wide event processing
 */
class EventHandlerSystem(
    private val fileTreeView: FileTreeView?,
    private val bottomBarView: BottomBarView?,
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

        UIActionHandler(parentStage).initialize()
        StatusActionHandler(bottomBarView).initialize()

        logger.info("Event handler system initialized")
        isInitialized = true
    }
}