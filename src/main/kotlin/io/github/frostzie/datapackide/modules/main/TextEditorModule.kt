package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.screen.elements.main.TextEditorView

class TextEditorModule(private val textEditorView: TextEditorView) {

    fun undo() {
        textEditorView.requestFocus()
        textEditorView.run {
            webView.engine.executeScript("window.editorUndo && window.editorUndo();")
        }
    }

    fun redo() {
        textEditorView.requestFocus()
        textEditorView.run {
            webView.engine.executeScript("window.editorRedo && window.editorRedo();")
        }
    }

    fun cut() {
        textEditorView.requestFocus()
        textEditorView.run {
            webView.engine.executeScript("window.editorCut && window.editorCut();")
        }
    }

    fun copy() {
        textEditorView.requestFocus()
        textEditorView.run {
            webView.engine.executeScript("window.editorCopy && window.editorCopy();")
        }
    }

    fun paste() {
        textEditorView.requestFocus()
        textEditorView.run {
            webView.engine.executeScript("window.editorPaste && window.editorPaste();")
        }
    }

    fun selectAll() {
        textEditorView.requestFocus()
        textEditorView.run {
            webView.engine.executeScript("window.editorSelectAll && window.editorSelectAll();")
        }
    }
}