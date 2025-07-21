package io.github.frostzie.datapackide.ui

import imgui.ImGui
import io.github.frostzie.datapackide.ui.UIConstants.LOCKED_WINDOW_FLAGS

/**
 * Renders the main Editor panel.
 * This will eventually host the MCEF browser view for the web-based VS Code editor.
 */
class EditorPanel : UiComponent {
    override fun render() {
        if (ImGui.begin("Editor", LOCKED_WINDOW_FLAGS)) {
            ImGui.text("Embedded Code Editor With MCEF")
        }
        ImGui.end()
    }
}