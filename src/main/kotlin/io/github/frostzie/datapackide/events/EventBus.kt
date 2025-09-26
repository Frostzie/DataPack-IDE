package io.github.frostzie.datapackide.events

import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

object EventBus {
    val logger = LoggerProvider.getLogger("EventBus")
    private val listener = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()
    private val handlerMap = mutableMapOf<Any, MutableList<Pair<Class<*>, (Any) -> Unit>>>()

    fun register(handler: Any) {
        if (handlerMap.containsKey(handler)) {
            logger.warn("Handler ${handler::class.simpleName} is alr registered")
            return
        }

        val methods = handler::class.memberFunctions.filter {
            it.findAnnotation<SubscribeEvent>() != null
        }

        val registered = mutableListOf<Pair<Class<*>, (Any) -> Unit>>()

        for (method in methods) {
            val parameters = method.parameters
            if (parameters.size != 2) continue

            val eventType = parameters[1].type.classifier as? KClass<*> ?: continue
            val eventClass = eventType.java

            method.isAccessible = true
            val fn: (Any) -> Unit = { event -> method.call(handler, event) }

            listener.getOrPut(eventClass) { mutableListOf() }.add(fn)
            registered += eventClass to fn

            logger.debug("Registered listener ${method.name} for event ${eventClass.simpleName}")
        }

        handlerMap[handler] = registered
        logger.info("REgistered handler ${handler::class.simpleName} with ${methods.size} subs")
    }

    fun unregister(handler: Any) {
        val registered = handlerMap.remove(handler) ?: return
        for ((eventClass, fn) in registered) {
            listener[eventClass]?.remove(fn)
        }
        logger.info("Unregistered handler ${handler::class.simpleName}")
    }

    fun post(event: Any) {
        listener[event::class.java]?.forEach {it(event)}
        logger.debug("Posted Event: ${event::class.simpleName}")
    }

    fun clear(event: Any) {
        listener.clear()
        handlerMap.clear()
        logger.debug("Cleared all event listeners")
    }
}