package io.github.frostzie.datapackide.handlers.main

import io.github.frostzie.datapackide.events.EditorCopy
import io.github.frostzie.datapackide.events.EditorCut
import io.github.frostzie.datapackide.events.EditorPaste
import io.github.frostzie.datapackide.events.EditorRedo
import io.github.frostzie.datapackide.events.EditorSelectAll
import io.github.frostzie.datapackide.events.EditorUndo
import io.github.frostzie.datapackide.modules.main.TextEditorModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

@Suppress("unused")
class TextEditorHandler(private val textEditorModule: TextEditorModule) {

    @SubscribeEvent
    fun onUndo(event: EditorUndo) {
        textEditorModule.undo()
    }

    @SubscribeEvent
    fun onRedo(event: EditorRedo) {
        textEditorModule.redo()
    }

    @SubscribeEvent
    fun onCut(event: EditorCut) {
        textEditorModule.cut()
    }

    @SubscribeEvent
    fun onCopy(event: EditorCopy) {
        textEditorModule.copy()
    }

    @SubscribeEvent
    fun onPaste(event: EditorPaste) {
        textEditorModule.paste()
    }

    @SubscribeEvent
    fun onSelectAll(event: EditorSelectAll) {
        textEditorModule.selectAll()
    }
}