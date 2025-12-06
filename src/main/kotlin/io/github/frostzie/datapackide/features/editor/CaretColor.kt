package io.github.frostzie.datapackide.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.settings.categories.MainConfig
import javafx.beans.value.ChangeListener
import org.fxmisc.richtext.CodeArea

/**
 * An implementation of [EditorTabDecorator] that changes the caret color
 * based on user settings.
 */
class CaretColor : EditorTabDecorator {

    override fun decorate(tab: Tab, tabData: TextEditorViewModel.TabData): () -> Unit {
        val configListener = ChangeListener<Any> { _, _, _ ->
            // Only update if it has focus.
            if (tabData.codeArea.isFocused) {
                updateCaretColor(tabData.codeArea)
            }
        }

        val focusListener = ChangeListener<Boolean> { _, _, isFocused ->
            if (isFocused) {
                updateCaretColor(tabData.codeArea)
            }
        }

        MainConfig.enableCaretColor.addListener(configListener)
        MainConfig.caretColor.addListener(configListener)
        tabData.codeArea.focusedProperty().addListener(focusListener)

        // Set initial color if it's already focused (e.g. first tab)
        if (tabData.codeArea.isFocused) {
            updateCaretColor(tabData.codeArea)
        }

        // Return cleanup function
        return {
            MainConfig.enableCaretColor.removeListener(configListener)
            MainConfig.caretColor.removeListener(configListener)
            tabData.codeArea.focusedProperty().removeListener(focusListener)
        }
    }

    private fun updateCaretColor(codeArea: CodeArea) {
        val caret = codeArea.lookup(".caret")
        if (MainConfig.enableCaretColor.get()) {
            val color = MainConfig.caretColor.get()
            caret?.style = "-fx-stroke: $color;"
        } else {
            caret?.style = ""
        }
    }
}