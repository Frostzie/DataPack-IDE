package io.github.frostzie.datapackide.events

import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.Path

//TODO: separate events into their own files

/**
 * Simple central event bus for DataPack IDE
 */
object EventBus {
    val logger = LoggerProvider.getLogger("EventBus")
    val listeners = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    /**
     * Register a listener for a specific event type
     */
    inline fun <reified T : Any> register(noinline listener: (T) -> Unit) {
        val eventClass = T::class.java
        listeners.getOrPut(eventClass) { mutableListOf() }
            .add { event -> listener(event as T) }
        logger.debug("Registered listener for ${eventClass.simpleName}")
    }

    /**
     * Post an event to all registered listeners
     */
    fun post(event: Any) {
        val eventClass = event::class.java
        listeners[eventClass]?.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                logger.error("Error handling event ${eventClass.simpleName}", e)
            }
        }
        logger.debug("Posted event: ${eventClass.simpleName}")
    }

    /**
     * Clear all listeners (useful for testing)
     */
    fun clear() {
        listeners.clear()
        logger.debug("Cleared all event listeners")
    }
}

/**
 * Event fired when a file should be opened
 */
data class FileOpenEvent(val filePath: Path)

/**
 * Event fired when a directory is selected for file tree
 */
data class DirectorySelectedEvent(val directoryPath: Path)