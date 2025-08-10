package io.github.frostzie.datapackide.screen
/*
import com.mojang.blaze3d.textures.TextureFormat
import io.github.frostzie.datapackide.utils.ReflectionUtils
import net.minecraft.client.texture.GlTexture
import org.lwjgl.opengl.GL11
import java.lang.reflect.Field

/**
 * A custom GlTexture that allows its ID and dimensions to be updated dynamically.
 */
class BrowserGlTexture(
    initialId: Int,
    label: String,
    initialWidth: Int,
    initialHeight: Int
) : GlTexture(
    GL11.GL_TEXTURE_2D,
    label,
    TextureFormat.RGBA8,
    initialWidth,
    initialHeight,
    1,
    1,
    initialId
) {
    private var mutableWidth: Int = initialWidth
    private var mutableHeight: Int = initialHeight

    companion object {
        private val idField: Field? = ReflectionUtils.findField(GlTexture::class.java, "glId")
    }

    override fun getWidth(i: Int): Int = this.mutableWidth shr i
    fun setWidth(width: Int) {
        this.mutableWidth = width
    }

    override fun getHeight(i: Int): Int = this.mutableHeight shr i
    fun setHeight(height: Int) {
        this.mutableHeight = height
    }

    /**
     * Uses reflection to set the protected 'id' field in the parent GlTexture class.
     */
    fun setGlId(newId: Int) {
        try {
            idField?.setInt(this, newId)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}
 */