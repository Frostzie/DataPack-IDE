package io.github.frostzie.datapackide.imgui

import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import imgui.ImGui
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

object ImGuiImpl {
    private val imGuiImplGlfw = ImGuiImplGlfw()
    private val imGuiImplGl3 = ImGuiImplGl3()

    fun create(handle: Long) {
        ImGui.createContext()
        ImPlot.createContext()

        val data = ImGui.getIO()
        data.iniFilename = "DataPack-IDE"
        data.fontGlobalScale = 1f

        // If you want to have custom fonts, you can use the following code here

//        {
//            final ImFontAtlas fonts = data.getFonts();
//            final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
//
//            rangesBuilder.addRanges(data.getFonts().getGlyphRangesDefault());
//            rangesBuilder.addRanges(data.getFonts().getGlyphRangesCyrillic());
//            rangesBuilder.addRanges(data.getFonts().getGlyphRangesJapanese());
//
//            final short[] glyphRanges = rangesBuilder.buildRanges();
//
//            final ImFontConfig basicConfig = new ImFontConfig();
//            basicConfig.setGlyphRanges(data.getFonts().getGlyphRangesCyrillic());
//
//            final List<ImFont> generatedFonts = new ArrayList<>();
//            for (int i = 5 /* MINIMUM_FONT_SIZE */; i <= 50 /* MAXIMUM_FONT_SIZE */; i++) {
//                basicConfig.setName("<Font Name> " + i + "px");
//                generatedFonts.add(fonts.addFontFromMemoryTTF(IOUtils.toByteArray(Objects.requireNonNull(ImGuiImpl.class.getResourceAsStream("<File Path>"))), i, basicConfig, glyphRanges));
//            }
//            fonts.build();
//            basicConfig.destroy();
//        }

        // The "generatedFonts" list now contains an ImFont for each scale from 5 to 50, you should save the font scales you want as global fields here to use them later:
        // For example:
        // defaultFont = generatedFonts.get(30); // Font scale is 30
        // How you can apply the font then, you can see in ExampleMixin
        data.configFlags = ImGuiConfigFlags.DockingEnable
        ImGuiConfigFlags.ViewportsEnable

        // In case you want to enable Viewports on Windows, you have to do this instead of the above line:
        // data.setConfigFlags(ImGuiConfigFlags.DockingEnable | ImGuiConfigFlags.ViewportsEnable);
        imGuiImplGlfw.init(handle, true)
        imGuiImplGl3.init()
    }

    fun draw(runnable: RenderInterface) {
        // Minecraft will not bind the framebuffer unless it is needed, so do it manually and hope Vulcan never gets real:tm:
        val framebuffer = MinecraftClient.getInstance().framebuffer
        val previousFramebuffer = (framebuffer.getColorAttachment() as GlTexture).getOrCreateFramebuffer(
            (RenderSystem.getDevice() as GlBackend).bufferManager,
            null
        )
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer)
        GL11.glViewport(0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight)

        // start frame
        imGuiImplGlfw.newFrame() // Handle keyboard and mouse interactions
        ImGui.newFrame()

        // do rendering logic
        runnable.render(ImGui.getIO())

        // end frame
        ImGui.render()
        imGuiImplGl3.renderDrawData(ImGui.getDrawData())

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer)

        // Add this code if you have enabled Viewports in the create method
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            val pointer = GLFW.glfwGetCurrentContext()
            ImGui.updatePlatformWindows()
            ImGui.renderPlatformWindowsDefault()

            GLFW.glfwMakeContextCurrent(pointer)
       }
    }

    fun dispose() {
        ImGui.destroyContext()
        ImPlot.destroyContext()
        imGuiImplGl3.dispose()
        imGuiImplGlfw.dispose()
    } // Can be used to load buffered images in ImGui
    //    public static int fromBufferedImage(BufferedImage image) {
    //        final int[] pixels = new int[image.getWidth() * image.getHeight()];
    //        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    //
    //        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
    //
    //        for (int y = 0; y < image.getHeight(); y++) {
    //            for (int x = 0; x < image.getWidth(); x++) {
    //                final int pixel = pixels[y * image.getWidth() + x];
    //
    //                buffer.put((byte) ((pixel >> 16) & 0xFF));
    //                buffer.put((byte) ((pixel >> 8) & 0xFF));
    //                buffer.put((byte) (pixel & 0xFF));
    //                buffer.put((byte) ((pixel >> 24) & 0xFF));
    //            }
    //        }
    //
    //        buffer.flip();
    //
    //        final int texture = GlStateManager._genTexture();
    //        GlStateManager._bindTexture(texture);
    //
    //        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    //        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    //
    //        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    //
    //        return texture;
    //    }
}