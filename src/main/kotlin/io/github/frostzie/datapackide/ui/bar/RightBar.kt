package io.github.frostzie.datapackide.ui.bar

import imgui.ImGui
import io.github.frostzie.datapackide.ui.UIConstants
import io.github.frostzie.datapackide.ui.UiComponent

class RightBar : UiComponent {
    override fun render() {
        if (ImGui.begin("Right Sidebar", UIConstants.FULLY_LOCKED_WINDOW_FLAGS)) {
            ImGui.text("Mod Related Bar")
        }
        ImGui.end()
    }
}