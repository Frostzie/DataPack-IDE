package io.github.frostzie.datapackide.handlers.bars.top

import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand
import io.github.frostzie.datapackide.events.AboutMod
import io.github.frostzie.datapackide.events.MainWindowClose
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowMinimize
import io.github.frostzie.datapackide.events.MainWindowRestore
import io.github.frostzie.datapackide.events.ReloadDatapack
import io.github.frostzie.datapackide.events.ToggleMenuControls
import io.github.frostzie.datapackide.modules.bars.top.TopBarModule
import io.github.frostzie.datapackide.screen.MainApplication
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
    fun onRestoreBack(event: MainWindowRestore) {
        topBarModule.restore()
    }

    @SubscribeEvent
    fun onClose(event: MainWindowClose) {
        MainApplication.hideMainWindow()
    }

    @SubscribeEvent
    fun reloadDatapack(event: ReloadDatapack) {
        ReloadDataPacksCommand.executeCommandButton()
    }

    @SubscribeEvent
    fun onToggleMenuControls(event: ToggleMenuControls) {
        topBarModule.toggleMenuControls()
    }

    @SubscribeEvent
    fun onAboutMod(event: AboutMod) {
        topBarModule.aboutMod()
    }
}