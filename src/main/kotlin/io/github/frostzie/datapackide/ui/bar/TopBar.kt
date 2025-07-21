package io.github.frostzie.datapackide.ui.bar

import imgui.ImGui
import io.github.frostzie.datapackide.ui.UIConstants
import io.github.frostzie.datapackide.ui.UiComponent

/**
 * Renders the bottom bar panel, typically used for status info or a console log.
 */
class TopBar : UiComponent {
    override fun render() {
        if (ImGui.begin("Top Bar", UIConstants.FULLY_LOCKED_WINDOW_FLAGS)) {
            ImGui.text("Project name + /reload buttons, ect..")
        }
        ImGui.end()
    }
}