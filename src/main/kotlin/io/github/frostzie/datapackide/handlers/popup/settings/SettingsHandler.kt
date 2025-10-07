package io.github.frostzie.datapackide.handlers.popup.settings

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class SettingsHandler(private val settingsModule: SettingsModule) {

    @SubscribeEvent
    fun onShowSettingsWindow(event: SettingsWindowOpen) {
        settingsModule.showSettingsWindow()
    }

    @SubscribeEvent
    fun onSettingsWindowDragStarted(event: SettingsWindowDragStarted) {
        settingsModule.xOffset = event.sceneX
        settingsModule.yOffset = event.sceneY
    }

    @SubscribeEvent
    fun onSettingsWindowDragged(event: SettingsWindowDragged) {
        settingsModule.dragWindow(event.screenX, event.screenY)
    }

    @SubscribeEvent
    fun onSettingsSearchQueryChanged(event: SettingsSearchQueryChanged) {
        settingsModule.search(event.query)
    }

    @SubscribeEvent
    fun onSettingsCategorySelected(event: SettingsCategorySelected) {
        settingsModule.selectCategory(event.item)
    }

    @SubscribeEvent
    fun onSettingsSearchResultSelected(event: SettingsSearchResultSelected) {
        settingsModule.selectSearchResult(event.result)
    }

    @SubscribeEvent
    fun onSettingsWindowCloseAndSave(event: SettingsWindowCloseSave) {
        settingsModule.saveSettings()
        settingsModule.closeSettings()
    }

    @SubscribeEvent
    fun onSettingsSave(event: SettingsSave) {
        settingsModule.saveSettings()
    }

    @SubscribeEvent
    fun onRequestSettingsCategories(event: RequestSettingsCategories) {
        settingsModule.loadAndSendCategories()
    }
}