package io.github.frostzie.datapackide.handlers.popup.settings

import io.github.frostzie.datapackide.events.SettingsWindowCloseSave
import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class SettingsHandler(private val settingsModule: SettingsModule) {
    @SubscribeEvent
    fun onSettingsSaveRequest(event: SettingsWindowCloseSave) {
        settingsModule.saveSettings()
    }

    @SubscribeEvent
    fun onSettingsCloseRequest(event: SettingsWindowCloseSave) {
        settingsModule.closeSettings()
    }
}