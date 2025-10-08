package io.github.frostzie.datapackide.handlers.popup.settings

import io.github.frostzie.datapackide.config.AssetsConfig
import io.github.frostzie.datapackide.events.ReloadTheme
import io.github.frostzie.datapackide.events.ResetDefaultTheme
import io.github.frostzie.datapackide.modules.popup.settings.ThemeModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.CSSManager

class ThemeHandler(private val themeModule: ThemeModule) {

    @SubscribeEvent
    fun onReloadTheme(event: ReloadTheme) {
        CSSManager.reloadAllStyles(*themeModule.scenes.toTypedArray())
    }

    @SubscribeEvent
    fun onResetDefaultTheme(event: ResetDefaultTheme) {
        AssetsConfig.forceTransferAllStyleAssets()
        CSSManager.reloadAllStyles(*themeModule.scenes.toTypedArray())
    }
}
