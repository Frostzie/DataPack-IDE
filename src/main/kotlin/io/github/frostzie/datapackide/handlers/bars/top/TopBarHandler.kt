package io.github.frostzie.datapackide.handlers.bars.top

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowClose
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowMinimize
import io.github.frostzie.datapackide.eventsOLD.ShowSettingsRequest
import io.github.frostzie.datapackide.eventsOLD.ToggleMenuBarRequest
import io.github.frostzie.datapackide.eventsOLD.UIAction
import io.github.frostzie.datapackide.eventsOLD.UIActionEvent
import io.github.frostzie.datapackide.modules.bars.top.TopBarModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class TopBarHandler(private val topBarModule: TopBarModule) {
    @SubscribeEvent
    fun onMinimize(event: MainWindowMinimize) {
        topBarModule.minimize()
    }

    @SubscribeEvent
    fun onToggleMaximize(event: MainWindowMaximize) {
        topBarModule.toggleMaximize()
    }

    @SubscribeEvent
    fun onClose(event: MainWindowClose) {
        topBarModule.close()
    }

    @SubscribeEvent
    fun onToggleMenuBar(event: ToggleMenuBarRequest) {
        topBarModule.toggleMenuBar()
    }

    @SubscribeEvent
    fun onShowSettings(event: ShowSettingsRequest) {
        EventBus.post(UIActionEvent(UIAction.SHOW_SETTINGS))
    }
}