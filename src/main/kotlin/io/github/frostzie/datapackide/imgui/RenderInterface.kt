package io.github.frostzie.datapackide.imgui

import imgui.ImGuiIO

fun interface RenderInterface {
    fun render(io: ImGuiIO)
}