package io.github.frostzie.datapackide.ui

import imgui.flag.ImGuiWindowFlags

object UIConstants {
    /**
     * These flags are applied to UI panels.
     * The layout will now be saved to layout.ini.
     */
    const val LOCKED_WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar or
            ImGuiWindowFlags.NoCollapse


    const val FULLY_LOCKED_WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar or
            ImGuiWindowFlags.NoCollapse or
            ImGuiWindowFlags.NoResize or
            ImGuiWindowFlags.NoMove or
            ImGuiWindowFlags.NoDocking
}