package io.github.frostzie.datapackide.handlers.popup

import io.github.frostzie.datapackide.eventsOLD.SettingsCategorySelectedEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsCloseRequestEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsSaveRequestEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsSearchEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsSearchResultSelectedEvent
import io.github.frostzie.datapackide.modules.popup.SettingsModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

class SettingsHandler(private val settingsModule: SettingsModule) {

    @SubscribeEvent
    fun onSettingsSaveRequest(event: SettingsSaveRequestEvent) {
        settingsModule.saveSettings()
    }

    @SubscribeEvent
    fun onSettingsCloseRequest(event: SettingsCloseRequestEvent) {
        settingsModule.closeSettings()
    }

    @SubscribeEvent
    fun onSettingsCategorySelected(event: SettingsCategorySelectedEvent) {
        settingsModule.handleCategorySelection(event.categoryItem)
    }

    @SubscribeEvent
    fun onSettingsSearch(event: SettingsSearchEvent) {
        settingsModule.handleSearch(event.query)
    }

    @SubscribeEvent
    fun onSettingsSearchResultSelected(event: SettingsSearchResultSelectedEvent) {
        settingsModule.handleSearchResultSelection(event.result)
    }
}