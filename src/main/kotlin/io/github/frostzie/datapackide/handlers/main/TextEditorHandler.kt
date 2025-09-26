package io.github.frostzie.datapackide.handlers.main

import io.github.frostzie.datapackide.eventsOLD.MenuAction
import io.github.frostzie.datapackide.eventsOLD.MenuActionEvent
import io.github.frostzie.datapackide.eventsOLD.MenuCategory
import io.github.frostzie.datapackide.modules.main.TextEditorModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider

class TextEditorHandler(private val textEditorModule: TextEditorModule) {
    companion object {
        private val logger = LoggerProvider.getLogger("TextEditorHandler")
    }

    @SubscribeEvent
    fun onMenuAction(event: MenuActionEvent) {
        if (event.category != MenuCategory.EDIT) return

        logger.debug("Handling edit menu action: {}", event.action)

        when (event.action) {
            MenuAction.UNDO -> textEditorModule.undo()
            MenuAction.REDO -> textEditorModule.redo()
            MenuAction.CUT -> textEditorModule.cut()
            MenuAction.COPY -> textEditorModule.copy()
            MenuAction.PASTE -> textEditorModule.paste()
            MenuAction.SELECT_ALL -> textEditorModule.selectAll()
            MenuAction.FIND -> logger.info("Find dialog requested")
            MenuAction.REPLACE -> logger.info("Replace dialog requested")
            else -> logger.warn("Unhandled edit menu action: ${event.action}")
        }
    }
}