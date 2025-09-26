package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.utils.LoggerProvider

//TODO: Eventually...
class TextEditorModule(private val textEditor: TextEditor?) {

    companion object {
        private val logger = LoggerProvider.getLogger("TextEditorModule")
    }

    fun undo() {
        textEditor?.undo()
        logger.debug("Undo action performed")
    }

    fun redo() {
        textEditor?.redo()
        logger.debug("Redo action performed")
    }

    fun cut() {
        textEditor?.cut()
        logger.debug("Cut action performed")
    }

    fun copy() {
        textEditor?.copy()
        logger.debug("Copy action performed")
    }

    fun paste() {
        textEditor?.paste()
        logger.debug("Paste action performed")
    }

    fun selectAll() {
        textEditor?.selectAll()
        logger.debug("Select All action performed")
    }
}