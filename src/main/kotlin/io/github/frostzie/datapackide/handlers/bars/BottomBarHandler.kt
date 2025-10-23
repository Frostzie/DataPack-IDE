package io.github.frostzie.datapackide.handlers.bars

import io.github.frostzie.datapackide.events.EditorCursorPosition
import io.github.frostzie.datapackide.modules.bars.BottomBarModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class BottomBarHandler(private val bottomBarModule: BottomBarModule) {
    @SubscribeEvent
    fun onCursorPositionChanged(event: EditorCursorPosition) {
        bottomBarModule.updateCursorPosition(event.line, event.column)
    }
}