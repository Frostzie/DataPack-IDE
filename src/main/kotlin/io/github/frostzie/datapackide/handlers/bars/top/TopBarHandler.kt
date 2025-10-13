package io.github.frostzie.datapackide.handlers.bars.top

import io.github.frostzie.datapackide.events.AboutModLink
import io.github.frostzie.datapackide.events.DiscordLink
import io.github.frostzie.datapackide.events.MainWindowClose
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowMinimize
import io.github.frostzie.datapackide.events.MainWindowRestore
import io.github.frostzie.datapackide.events.MainWindowToggleMaximize
import io.github.frostzie.datapackide.events.ReloadDatapack
import io.github.frostzie.datapackide.events.ToggleMenuControls
import io.github.frostzie.datapackide.modules.bars.top.TopBarModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class TopBarHandler(private val topBarModule: TopBarModule) {

    @SubscribeEvent
    fun onMinimize(event: MainWindowMinimize) {
        topBarModule.minimize()
    }

    @SubscribeEvent
    fun onMaximize(event: MainWindowMaximize) {
        topBarModule.maximize()
    }

    @SubscribeEvent
    fun onToggleMaximize(event: MainWindowToggleMaximize) {
        topBarModule.toggleMaximize()
    }

    @SubscribeEvent
    fun onRestoreBack(event: MainWindowRestore) {
        topBarModule.restore()
    }

    @SubscribeEvent
    fun onClose(event: MainWindowClose) {
        topBarModule.hideWindow()
    }

    @SubscribeEvent
    fun reloadDatapack(event: ReloadDatapack) {
        topBarModule.reloadDatapacks()
    }

    @SubscribeEvent
    fun onToggleMenuControls(event: ToggleMenuControls) {
        topBarModule.toggleMenuControls()
    }

    @SubscribeEvent
    fun onAboutModLink(event: AboutModLink) {
        topBarModule.aboutModLink()
    }

    @SubscribeEvent
    fun onDiscordLink(event: DiscordLink) {
        topBarModule.discordLink()
    }
}