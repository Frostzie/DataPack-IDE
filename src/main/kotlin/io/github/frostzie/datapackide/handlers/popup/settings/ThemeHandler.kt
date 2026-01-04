package io.github.frostzie.datapackide.handlers.popup.settings

import io.github.frostzie.datapackide.events.OpenThemeEvent
import io.github.frostzie.datapackide.events.ReloadCSSEvent
import io.github.frostzie.datapackide.modules.popup.settings.ThemeModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.events.ThemeChangeEvent
import io.github.frostzie.datapackide.utils.ThemeManager
import io.github.frostzie.datapackide.utils.ThemeUtils

class ThemeHandler(private val themeModule: ThemeModule) {

    @SubscribeEvent
    fun onThemeChange(event: ThemeChangeEvent) {
        ThemeUtils.applyTheme(event.themeName)
    }

    @SubscribeEvent
    fun onReloadTheme(event: ReloadCSSEvent) {
        CSSManager.reloadAllStyles(*themeModule.scenes.toTypedArray())
    }

    @SubscribeEvent
    fun onOpenTheme(event: OpenThemeEvent) {
        ThemeManager.openTheme(event)
    }
}
