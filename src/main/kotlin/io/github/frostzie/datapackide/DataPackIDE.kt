package io.github.frostzie.datapackide

import io.github.frostzie.datapackide.screen.ExampleScreen
import io.github.frostzie.datapackide.ui.DemoScreen
import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class DataPackIDE : ModInitializer {

    companion object {
        val LOGGER = LoggerProvider.getLogger("DataPack-IDE")

        private val openScreenKeybind = KeyBinding(
            "Open Screen",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "Data Pack IDE"
        )

        private val demoScreenKeybind = KeyBinding(
            "Demo Screen",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "Data Pack IDE"
        )
    }

    override fun onInitialize() {
        LOGGER.info("DataPack IDE Initialized!")

        KeyBindingHelper.registerKeyBinding(openScreenKeybind)
        KeyBindingHelper.registerKeyBinding(demoScreenKeybind)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (openScreenKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(ExampleScreen())
                }
            }
            // TODO: Remove
            if (demoScreenKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(DemoScreen())
                }
            }
        }
    }
}