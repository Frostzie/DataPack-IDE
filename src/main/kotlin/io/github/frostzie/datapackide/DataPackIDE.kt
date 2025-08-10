package io.github.frostzie.datapackide

import io.github.frostzie.datapackide.commands.DefaultCommands
import io.github.frostzie.datapackide.screen.JavaFXTestWindow
import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class DataPackIDE : ModInitializer {
    private val logger = LoggerProvider.getLogger("DataPackIDE")

    override fun onInitialize() {
        logger.info("Pre-initializing JavaFX platform...")
        JavaFXTestWindow.initializeJavaFX()
        DefaultCommands.openMainScreen()

        toggleJavaFXKey = KeyBindingHelper.registerKeyBinding(KeyBinding(
            "key.datapack-ide.toggle_javafx",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.datapack-ide.general"
        ))

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (toggleJavaFXKey?.wasPressed() == true) {
                logger.info("JavaFX toggle keybind pressed!")
                JavaFXTestWindow.toggleTestWindow()
            }
        }
    }

    companion object {
        private var toggleJavaFXKey: KeyBinding? = null
    }
}