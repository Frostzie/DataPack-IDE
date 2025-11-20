package io.github.frostzie.datapackide.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.settings.categories.MainConfig
import javafx.beans.value.ChangeListener
import javafx.scene.control.Label
import javafx.scene.layout.HBox

/**
 * An implementation of [EditorTabDecorator] that adds a "dirty" suffix
 * to a tab when its file has unsaved changes.
 */
class DirtyTabDecorator : EditorTabDecorator {

    override fun decorate(tab: Tab, tabData: TextEditorViewModel.TabData): () -> Unit {
        // This listener reacts to both the dirty status and changes to the indicator setting itself.
        val listener = ChangeListener<Any> { _, _, _ ->
            updateTab(tab, tabData)
        }

        tabData.isDirty.addListener(listener)
        MainConfig.dirtyIndicator.addListener(listener)

        // Set the initial state correctly
        updateTab(tab, tabData)

        // Cleanup when the tab is closed
        return {
            tabData.isDirty.removeListener(listener)
            MainConfig.dirtyIndicator.removeListener(listener)
        }
    }

    private fun updateTab(tab: Tab, tabData: TextEditorViewModel.TabData) {
        val graphic = tab.graphic as? HBox ?: return
        val label = graphic.children.find { it is Label } as? Label ?: return

        val isDirty = tabData.isDirty.get()
        label.text = if (isDirty) {
            "${tabData.displayName}${MainConfig.dirtyIndicator.get()}"
        } else {
            tabData.displayName
        }
    }
}
