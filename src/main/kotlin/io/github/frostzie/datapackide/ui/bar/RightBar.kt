package io.github.frostzie.datapackide.ui.bar

import imgui.ImGui
import io.github.frostzie.datapackide.ui.UIConstants
import io.github.frostzie.datapackide.ui.UiComponent
import io.github.frostzie.datapackide.ui.settings.SettingsPanel

class RightBar : UiComponent {
    override fun render() {
        if (ImGui.begin("Right Sidebar", UIConstants.FULLY_LOCKED_WINDOW_FLAGS)) {
            ImGui.text("Mod Related Bar")

            val availableHeight = ImGui.getContentRegionAvail().y
            val buttonHeight = ImGui.getFrameHeightWithSpacing()
            if (availableHeight > buttonHeight) {
                ImGui.setCursorPosY(ImGui.getCursorPosY() + availableHeight - buttonHeight)
            }

            if (ImGui.button("Settings", -1f, 0f)) {
                SettingsPanel.show.set(true)
            }
        }
        ImGui.end()
    }
}