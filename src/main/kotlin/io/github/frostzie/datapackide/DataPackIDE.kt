package io.github.frostzie.datapackide

import io.github.frostzie.datapackide.commands.DefaultCommands
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.loader.fabric.Keybinds
import io.github.frostzie.datapackide.screen.MainApplication
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

        ConfigManager.initialize()
        DefaultCommands.registerCommands()
        Keybinds.register()
    }
}