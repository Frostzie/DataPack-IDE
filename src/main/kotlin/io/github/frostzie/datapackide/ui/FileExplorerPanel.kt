package io.github.frostzie.datapackide.ui

import imgui.ImGui
import io.github.frostzie.datapackide.ui.UIConstants.LOCKED_WINDOW_FLAGS

/**
 * Renders the File Explorer panel.
 * This will eventually contain a tree view of the project files.
 */
class FileExplorerPanel : UiComponent {
    override fun render() {
        if (ImGui.begin("File Explorer", LOCKED_WINDOW_FLAGS)) {
            ImGui.text("File Explorer Panel")
        }
        ImGui.end()
    }
}