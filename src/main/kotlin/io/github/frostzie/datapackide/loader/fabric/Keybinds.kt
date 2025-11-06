package io.github.frostzie.datapackide.loader.fabric

import io.github.frostzie.datapackide.screen.MainApplication
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

object Keybinds {
    private val logger = LoggerProvider.getLogger("DataPackIDE:Keybinds")
    private var toggleIDEKey: KeyBinding? = null

    fun register() {
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
                    MainApplication.showMainWindow()
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
}
