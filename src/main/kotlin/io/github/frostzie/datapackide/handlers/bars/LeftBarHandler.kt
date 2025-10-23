package io.github.frostzie.datapackide.handlers.bars

import io.github.frostzie.datapackide.events.ChooseDirectory
import io.github.frostzie.datapackide.modules.bars.LeftBarModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class LeftBarHandler(private val leftBarModule: LeftBarModule) {

    @SubscribeEvent
    fun onChooseDirectory(event: ChooseDirectory) {
        leftBarModule.openDirectoryChooser()
    }
}