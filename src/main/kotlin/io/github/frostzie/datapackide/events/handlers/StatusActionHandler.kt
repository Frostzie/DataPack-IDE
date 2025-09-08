package io.github.frostzie.datapackide.events.handlers

import io.github.frostzie.datapackide.events.EditorCursorChangedEvent
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.StatusType
import io.github.frostzie.datapackide.events.StatusUpdateEvent
import io.github.frostzie.datapackide.screen.elements.bars.StatusBar
import io.github.frostzie.datapackide.utils.LoggerProvider

/**
 * Handles status bar updates based on various events.
 */
class StatusActionHandler(private val statusBar: StatusBar?) {
    companion object {
        private val logger = LoggerProvider.getLogger("StatusActionHandler")
    }

    fun initialize() {
        EventBus.register<EditorCursorChangedEvent> { event ->
            statusBar?.updateCursorPosition(event.line, event.column)
        }

        EventBus.register<StatusUpdateEvent> { event ->
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