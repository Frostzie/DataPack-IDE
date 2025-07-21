package io.github.frostzie.datapackide.ui.bar

import imgui.ImGui
import io.github.frostzie.datapackide.ui.UIConstants
import io.github.frostzie.datapackide.ui.UiComponent

class LeftBar : UiComponent {
    override fun render() {
        if (ImGui.begin("Left Sidebar", UIConstants.FULLY_LOCKED_WINDOW_FLAGS)) {
            ImGui.text("Datapack Related Bar")
        }
        ImGui.end()
    }
}