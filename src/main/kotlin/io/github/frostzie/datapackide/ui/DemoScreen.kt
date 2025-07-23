package io.github.frostzie.datapackide.ui

import imgui.ImGui
import io.github.frostzie.datapackide.imgui.ImGuiImpl
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

/**
 * A screen that displays the ImGui demo window.
 * Useful for debugging and exploring ImGui features.
 */
class DemoScreen : Screen(Text.literal("ImGui Demo")) {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        ImGuiImpl.draw {
            ImGui.showDemoWindow()
        }
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
    }

    override fun shouldPause(): Boolean = true

    fun shouldRenderBehindWhenPaused(): Boolean = true
}