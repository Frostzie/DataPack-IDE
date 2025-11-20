package io.github.frostzie.datapackide.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.settings.categories.MainConfig
import javafx.beans.value.ChangeListener
import javafx.scene.control.Label
import javafx.scene.layout.HBox

/**
 * An implementation of [EditorTabDecorator] that changes the text color of a tab
 * when its underlying file has unsaved changes.
 */
class DirtyTextColorDecorator : EditorTabDecorator {

    override fun decorate(tab: Tab, tabData: TextEditorViewModel.TabData): () -> Unit {
        // A single listener to react to changes in dirty status or the configured color
        val listener = ChangeListener<Any> { _, _, _ ->
            updateTabStyle(tab, tabData)
        }

        tabData.isDirty.addListener(listener)
        MainConfig.dirtyFileColor.addListener(listener)

        // Apply the initial style
        updateTabStyle(tab, tabData)

        // Cleanup when the tab is closed
        return {
            tabData.isDirty.removeListener(listener)
            MainConfig.dirtyFileColor.removeListener(listener)
            // Reset the style to default on cleanup
            val graphic = tab.graphic as? HBox
            val label = graphic?.children?.find { it is Label } as? Label
            label?.style = ""
        }
    }

    private fun updateTabStyle(tab: Tab, tabData: TextEditorViewModel.TabData) {
        val graphic = tab.graphic as? HBox ?: return
        val label = graphic.children.find { it is Label } as? Label ?: return

        if (tabData.isDirty.get()) {
            val color = MainConfig.dirtyFileColor.get()
            label.style = "-fx-text-fill: $color;"
        } else {
            // Reset to default
            label.style = ""
        }
    }
}
