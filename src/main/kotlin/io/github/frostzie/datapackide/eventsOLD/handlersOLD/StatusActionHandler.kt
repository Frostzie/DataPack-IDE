package io.github.frostzie.datapackide.eventsOLD.handlersOLD

import io.github.frostzie.datapackide.eventsOLD.EditorCursorChangedEvent
import io.github.frostzie.datapackide.eventsOLD.EventBusOLD
import io.github.frostzie.datapackide.eventsOLD.StatusType
import io.github.frostzie.datapackide.eventsOLD.StatusUpdateEvent
import io.github.frostzie.datapackide.screen.elements.bars.StatusBar
import io.github.frostzie.datapackide.utils.LoggerProvider

@Deprecated("Replacing with newer system")
/**
 * Handles status bar updates based on various events.
 */
class StatusActionHandler(private val statusBar: StatusBar?) {
    companion object {
        private val logger = LoggerProvider.getLogger("StatusActionHandler")
    }

    fun initialize() {
        EventBusOLD.register<EditorCursorChangedEvent> { event ->
            statusBar?.updateCursorPosition(event.line, event.column)
        }

        EventBusOLD.register<StatusUpdateEvent> { event ->
            handleStatusUpdate(event)
        }
        logger.info("StatusActionHandler initialized")
    }

    //TODO: Fix since it doesn't work
    private fun handleStatusUpdate(event: StatusUpdateEvent) {
        when (event.type) {
            StatusType.CURSOR_POSITION -> {
                val line = event.data["line"] as? Int ?: 0
                val column = event.data["column"] as? Int ?: 0
                statusBar?.updateCursorPosition(line, column)
            }
            else -> logger.warn("Unhandled status update type: ${event.type}")
        }
    }
}