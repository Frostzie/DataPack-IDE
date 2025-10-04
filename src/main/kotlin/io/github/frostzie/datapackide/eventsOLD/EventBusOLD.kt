package io.github.frostzie.datapackide.eventsOLD

import io.github.frostzie.datapackide.utils.LoggerProvider

@Deprecated("Replacing with newer system")
/**
 * Simple central event bus for DataPack IDE
 * Handles registration and posting of all application events
 */
object EventBusOLD {
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
}