package io.github.frostzie.datapackide.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    private const val MAIN = "datapack-ide"
    private val loggers = mutableMapOf<String, Logger>()

    fun getLogger(name: String): Logger {
        val fullName = "$MAIN:$name"
        return loggers.getOrPut(fullName) {
            LoggerFactory.getLogger(fullName)
        }
    }
}