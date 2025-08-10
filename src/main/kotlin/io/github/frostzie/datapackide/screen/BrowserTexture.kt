package io.github.frostzie.datapackide.screen
/*
import com.mojang.blaze3d.systems.GpuDevice
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import io.github.frostzie.datapackide.utils.ReflectionUtils
import net.minecraft.client.texture.AbstractTexture
import java.lang.reflect.Field

/**
 * An AbstractTexture that wraps our custom BrowserGlTexture.
 */
class BrowserTexture(
    initialId: Int,
    label: String,
    width: Int,
    height: Int
) : AbstractTexture() {
    private val browserGlTexture: BrowserGlTexture = BrowserGlTexture(initialId, label, width, height)

    companion object {
        private val textureField: Field? = ReflectionUtils.findField(AbstractTexture::class.java, "glTexture")
        private val textureViewField: Field? = ReflectionUtils.findField(AbstractTexture::class.java, "glTextureView")
    }

    init {
        this.browserGlTexture.setTextureFilter(FilterMode.NEAREST, false)

        try {
            textureField?.set(this, this.browserGlTexture)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        val device: GpuDevice = RenderSystem.getDevice()
        val newTextureView = device.createTextureView(this.browserGlTexture)

        try {
            textureViewField?.set(this, newTextureView)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    fun setId(id: Int) {
        this.browserGlTexture.setGlId(id)
    }

    fun setWidth(width: Int) {
        this.browserGlTexture.setWidth(width)
    }

    fun setHeight(height: Int) {
        this.browserGlTexture.setHeight(height)
    }
}
 */