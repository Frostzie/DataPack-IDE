package io.github.frostzie.datapackide.ui.settings

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.type.ImBoolean
import io.github.frostzie.datapackide.ui.UIConstants.SETTINGS_WINDOW_FLAGS
import io.github.frostzie.datapackide.ui.UiComponent

/**
 * Renders the settings panel.
 * This panel can be opened from other UI components.
 */
class SettingsPanel : UiComponent {
    companion object {
        /**
         * Controls the visibility of the settings panel.
         * Use `SettingsPanel.show.set(true)` to open the panel.
         * The panel can be closed by the user via the UI.
         */
        val show = ImBoolean(false)
    }

    override fun render() {
        if (show.get()) {
            val mainViewport = ImGui.getMainViewport()
            ImGui.setNextWindowPos(
                mainViewport.posX + mainViewport.sizeX * 0.5f,
                mainViewport.posY + mainViewport.sizeY * 0.5f,
                ImGuiCond.Appearing,
                0.5f,
                0.5f
            )
            ImGui.setNextWindowFocus()

            if (ImGui.begin("Settings", show, SETTINGS_WINDOW_FLAGS)) {
                ImGui.text("Mod Settings")

                // TODO: Add actual settings controls here
            }
            ImGui.end()
        }
    }
}