package io.github.frostzie.datapackide

import io.github.frostzie.datapackide.commands.DefaultCommands
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.screen.MainApplication
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

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

        toggleIDEKey = KeyBindingHelper.registerKeyBinding(KeyBinding(
            "key.datapack-ide.toggle_ide",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.datapack-ide.general"
        ))

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (toggleIDEKey?.wasPressed() == true) {
                logger.info("IDE toggle keybind pressed!")

                if (JavaFXInitializer.isJavaFXAvailable()) {
                    MainApplication.toggleMainWindow()
                } else {
                    logger.error("Cannot open IDE window - JavaFX is not available")
                    client.player?.sendMessage(
                        Text.literal("§cDataPack IDE: JavaFX not available - cannot open GUI"),
                        false
                    )
                }
            }
        }
    }

    companion object {
        private var toggleIDEKey: KeyBinding? = null
    }
}