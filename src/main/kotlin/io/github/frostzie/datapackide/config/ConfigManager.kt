package io.github.frostzie.datapackide.config

import io.github.frostzie.datapackide.project.WorkspaceManager
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import io.github.frostzie.datapackide.settings.categories.ExampleConfig
import io.github.frostzie.datapackide.settings.categories.MainConfig
import io.github.frostzie.datapackide.settings.categories.MinecraftConfig
import io.github.frostzie.datapackide.settings.categories.ThemeConfig
import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

//TODO: Allow selecting your own config dir / or set it to AppData to work with any instance
/**
 * The Main configuration manager for DataPack IDE
 * Handles initialization and management of all config subsystems
 */
object ConfigManager {

    private val logger = LoggerProvider.getLogger("ConfigManager")

    val configDir: Path = FabricLoader.getInstance().configDir.resolve("datapack-ide") //TODO: Move to Loader specific

    fun initialize() {
        if (!configDir.toFile().exists()) {
            configDir.toFile().mkdirs()
            logger.debug("Created config directory: {}", configDir)
        }

        LayoutManager.initialize()
        WorkspaceManager.initialize()

        SettingsManager.register("main", MainConfig::class)
        SettingsManager.register("theme", ThemeConfig::class)
        SettingsManager.register("minecraft", MinecraftConfig::class)
        SettingsManager.register("advanced", AdvancedConfig::class)
        SettingsManager.register("example", ExampleConfig::class)

        SettingsManager.initialize()
    }
}