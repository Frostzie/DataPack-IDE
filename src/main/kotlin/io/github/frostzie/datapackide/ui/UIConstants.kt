package io.github.frostzie.datapackide.ui

import imgui.flag.ImGuiTabBarFlags
import imgui.flag.ImGuiWindowFlags

object UIConstants {
    /**
     * For Main Panels that should allow movement and resizing.
     */
    const val LOCKED_WINDOW_FLAGS = ImGuiWindowFlags.NoCollapse or
            ImGuiTabBarFlags.NoTooltip or
            ImGuiWindowFlags.NoCollapse or
            ImGuiTabBarFlags.NoCloseWithMiddleMouseButton or
            ImGuiWindowFlags.NoNav

    /**
     * For Bars and non-editable panels.
     */
    const val FULLY_LOCKED_WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar or
            ImGuiWindowFlags.NoCollapse or
            ImGuiWindowFlags.NoResize or
            ImGuiWindowFlags.NoMove or
            ImGuiWindowFlags.NoDocking

    /**
     * For settings panels.
     */
    const val SETTINGS_WINDOW_FLAGS = ImGuiWindowFlags.NoCollapse or
            ImGuiWindowFlags.AlwaysAutoResize or
            ImGuiWindowFlags.NoDocking

}