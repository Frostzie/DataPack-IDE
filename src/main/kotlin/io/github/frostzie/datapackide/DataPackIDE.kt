package io.github.frostzie.datapackide

import io.github.frostzie.datapackide.ingame.DefaultCommands
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.loader.fabric.Keybinds
import io.github.frostzie.datapackide.modules.universal.UniversalWorldIntegration
import io.github.frostzie.datapackide.screen.MainApplication
import io.github.frostzie.datapackide.settings.SettingsLoader
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.api.ModInitializer

class DataPackIDE : ModInitializer {
    private val logger = LoggerProvider.getLogger("DataPackIDE")

    override fun onInitialize() {
        System.setProperty("javafx.allowSystemPropertiesAccess", "true")
        System.setProperty("javafx.platform", "desktop")

        if (JavaFXInitializer.isJavaFXAvailable()) {
            logger.info("Pre-initializing JavaFX platform...")
            MainApplication.initializeJavaFX()
        } else {
            logger.warn("JavaFX is not available - GUI features will be disabled")
        }

        ConfigManager.initialize() // Loads config file management and Layout system and Workspace data
        SettingsLoader.initialize() // Loads settings and their builder and universal logic
        DefaultCommands.registerCommands() // Loads commands
        Keybinds.register() // Loads Minecraft (Fabric) keybinds
        UniversalWorldIntegration.initialize() // Loads world detection for universal datapacks
    }
}