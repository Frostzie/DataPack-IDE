package io.github.frostzie.datapackide.config

import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

/**
 * The Main configuration manager for DataPack IDE
 * Handles initialization and management of all config subsystems
 */
object ConfigManager {

    private val logger = LoggerProvider.getLogger("ConfigManager")

    val configDir: Path = FabricLoader.getInstance().configDir.resolve("datapack-ide")

    fun initialize() {
        logger.info("Initializing ConfigManager...")

        if (!configDir.toFile().exists()) {
            configDir.toFile().mkdirs()
            logger.info("Created config directory: $configDir")
        }

        AssetsConfig.initialize()
        WebsiteConfig.initialize()

        logger.info("ConfigManager initialization complete")
    }
}