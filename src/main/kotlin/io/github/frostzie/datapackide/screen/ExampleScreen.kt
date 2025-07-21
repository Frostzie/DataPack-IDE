package io.github.frostzie.datapackide.screen

import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import io.github.frostzie.datapackide.imgui.ImGuiImpl
import io.github.frostzie.datapackide.ui.EditorPanel
import io.github.frostzie.datapackide.ui.FileExplorerPanel
import io.github.frostzie.datapackide.ui.bar.LeftBar
import io.github.frostzie.datapackide.ui.bar.RightBar
import io.github.frostzie.datapackide.ui.bar.TopBar
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ExampleScreen : Screen(Text.literal("Example Screen")) {

    private val topBarHeight = 30f
    private val leftBarWidth = 60f
    private val rightBarWidth = 60f

    private val topBar = TopBar()
    private val leftBar = LeftBar()
    private val rightBar = RightBar()
    private val fileExplorer = FileExplorerPanel()
    private val editorPanel = EditorPanel()

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        ImGuiImpl.draw {
            val mainViewport = ImGui.getMainViewport()
            val workPos = mainViewport.workPos
            val workSize = mainViewport.workSize

            // Top Bar
            ImGui.setNextWindowPos(workPos.x, workPos.y)
            ImGui.setNextWindowSize(workSize.x, topBarHeight)
            topBar.render()

            // Left Bar
            ImGui.setNextWindowPos(workPos.x, workPos.y + topBarHeight)
            ImGui.setNextWindowSize(leftBarWidth, workSize.y - topBarHeight)
            leftBar.render()

            // Right Bar
            ImGui.setNextWindowPos(workPos.x + workSize.x - rightBarWidth, workPos.y + topBarHeight)
            ImGui.setNextWindowSize(rightBarWidth, workSize.y - topBarHeight)
            rightBar.render()

            val centralX = workPos.x + leftBarWidth
            val centralY = workPos.y + topBarHeight
            val centralWidth = workSize.x - leftBarWidth - rightBarWidth
            val centralHeight = workSize.y - topBarHeight

            ImGui.setNextWindowPos(centralX, centralY)
            ImGui.setNextWindowSize(centralWidth, centralHeight)

            val hostWindowFlags = ImGuiWindowFlags.NoDocking or
                    ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoCollapse or
                    ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoMove or
                    ImGuiWindowFlags.NoBringToFrontOnFocus or ImGuiWindowFlags.NoNavFocus

            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f)
            ImGui.begin("Central Dockspace Host", hostWindowFlags)
            ImGui.popStyleVar()

            val dockspaceId = ImGui.getID("CentralDockSpace")
            ImGui.dockSpace(dockspaceId)

            fileExplorer.render()
            editorPanel.render()

            ImGui.end()
        }
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Do nothing for a transparent background
    }

    override fun shouldPause(): Boolean = true

    fun shouldRenderBehindWhenPaused(): Boolean = true
}