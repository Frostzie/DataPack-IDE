package io.github.frostzie.datapackide.imgui

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import imgui.ImGui
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import io.github.frostzie.datapackide.config.DefaultLayout
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

object ImGuiImpl {
    private val imGuiImplGlfw = ImGuiImplGlfw()
    private val imGuiImplGl3 = ImGuiImplGl3()

    fun create(handle: Long) {
        ImGui.createContext()
        ImPlot.createContext()

        val io = ImGui.getIO()

        val iniPath = DefaultLayout.getAndEnsureLayoutIniPath()
        io.iniFilename = iniPath.toAbsolutePath().toString()

        io.fontGlobalScale = 1.0f
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)

        imGuiImplGlfw.init(handle, true)
        imGuiImplGl3.init()
    }

    fun draw(runnable: RenderInterface) {
        val framebuffer = MinecraftClient.getInstance().framebuffer
        val previousFramebuffer = (framebuffer.colorAttachment as GlTexture).getOrCreateFramebuffer(
            (RenderSystem.getDevice() as GlBackend).bufferManager, null
        )

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer)
        GL11.glViewport(0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight)

        imGuiImplGl3.newFrame()
        imGuiImplGlfw.newFrame()
        ImGui.newFrame()

        runnable.render(ImGui.getIO())

        ImGui.render()
        imGuiImplGl3.renderDrawData(ImGui.getDrawData())

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer)
    }

    fun dispose() {
        imGuiImplGl3.shutdown()
        imGuiImplGlfw.shutdown()

        ImPlot.destroyContext()
        ImGui.destroyContext()
    }
}