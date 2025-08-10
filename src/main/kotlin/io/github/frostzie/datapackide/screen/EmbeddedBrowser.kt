package io.github.frostzie.datapackide.screen
/*
import com.cinemamod.mcef.MCEF
import com.cinemamod.mcef.MCEFBrowser
import io.github.frostzie.datapackide.config.WebManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class EmbeddedBrowser : Screen(Text.literal("Embedded Browser")) {
    companion object {
        private const val BROWSER_DRAW_OFFSET = 20
        private val BROWSER_TEXTURE_ID = Identifier.of("datapackide", "browser_texture")
    }

    private var browser: MCEFBrowser? = null
    private val minecraft = MinecraftClient.getInstance()

    private var browserTexture: BrowserTexture? = null
    private var lastTextureId: Int = 0

    override fun init() {
        super.init()
        if (browser == null) {
            val url = WebManager.mainHtmlUrl
            val transparent = false
            browser = MCEF.createBrowser(url, transparent)

            browserTexture = BrowserTexture(0, "mcef_browser", 1, 1)
            minecraft.textureManager.registerTexture(BROWSER_TEXTURE_ID, browserTexture!!)

            resizeBrowser()
        }
    }

    private fun mouseX(x: Double): Int {
        return ((x - BROWSER_DRAW_OFFSET) * minecraft.window.scaleFactor).toInt()
    }

    private fun mouseY(y: Double): Int {
        return ((y - BROWSER_DRAW_OFFSET) * minecraft.window.scaleFactor).toInt()
    }

    private fun scaleX(x: Double): Int {
        return ((x - BROWSER_DRAW_OFFSET * 2) * minecraft.window.scaleFactor).toInt()
    }

    private fun scaleY(y: Double): Int {
        return ((y - BROWSER_DRAW_OFFSET * 2) * minecraft.window.scaleFactor).toInt()
    }

    private fun resizeBrowser() {
        browser?.let { browser ->
            if (width > BROWSER_DRAW_OFFSET * 2 && height > BROWSER_DRAW_OFFSET * 2) {
                val newWidth = scaleX(width.toDouble())
                val newHeight = scaleY(height.toDouble())
                browser.resize(newWidth, newHeight)

                browserTexture?.setWidth(newWidth)
                browserTexture?.setHeight(newHeight)
            }
        }
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        resizeBrowser()
    }

    override fun close() {
        browser?.close()
        browser = null

        if (browserTexture != null) {
            minecraft.textureManager.destroyTexture(BROWSER_TEXTURE_ID)
            browserTexture = null
        }

        super.close()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        browser?.let { browser ->
            val textureId = browser.renderer.textureID
            if (textureId != 0 && browserTexture != null) {
                if (textureId != lastTextureId) {
                    browserTexture!!.setId(textureId)
                    lastTextureId = textureId
                }

                val x1 = BROWSER_DRAW_OFFSET
                val y1 = BROWSER_DRAW_OFFSET
                val x2 = width - BROWSER_DRAW_OFFSET
                val y2 = height - BROWSER_DRAW_OFFSET

                val u1 = 0.0f
                val u2 = 1.0f
                val v1 = 0.0f
                val v2 = 1.0f

                context.drawTexturedQuad(BROWSER_TEXTURE_ID, x1, y1, x2, y2, u1, u2, v1, v2)
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        browser?.let {
            it.sendMousePress(mouseX(mouseX), mouseY(mouseY), button)
            it.setFocus(true)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        browser?.let {
            it.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button)
            it.setFocus(true)
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        browser?.sendMouseMove(mouseX(mouseX), mouseY(mouseY))
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        browser?.sendMouseMove(mouseX(mouseX), mouseY(mouseY))
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        browser?.let {
            val scrollMultiplier = 50.0 // Use a double for multiplication

            // BUG FIX: Based on the MCEFBrowser code you provided, sendMouseWheel only takes one scroll value.
            // We prioritize vertical scrolling as it's most common.
            // We also pass 0 for the modifiers, as they aren't available in this method.
            if (verticalAmount != 0.0) {
                it.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), verticalAmount * scrollMultiplier, 0)
            }
            if (horizontalAmount != 0.0) {
                // Note: The provided MCEF wrapper doesn't seem to support horizontal scrolling directly.
                // This call might not work as expected, but we include it for completeness.
                // A more advanced wrapper would be needed for true horizontal scroll.
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        browser?.let {
            it.sendKeyPress(keyCode, scanCode.toLong(), modifiers)
            it.setFocus(true)
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        browser?.let {
            it.sendKeyRelease(keyCode, scanCode.toLong(), modifiers)
            it.setFocus(true)
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (codePoint.code == 0) return false
        browser?.let {
            it.sendKeyTyped(codePoint, modifiers)
            it.setFocus(true)
        }
        return super.charTyped(codePoint, modifiers)
    }
}
 */