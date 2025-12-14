package io.github.frostzie.datapackide.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.paint.Color

//TODO: Remove. Make a system compatible with future plugins.
class ConfigTabDecorator : EditorTabDecorator {
    override fun decorate(tab: Tab, tabData: TextEditorViewModel.TabData): () -> Unit {
        val configDirPath = ConfigManager.configDir.toAbsolutePath().toString()
        val filePath = tabData.filePath.toAbsolutePath().toString()

        if (filePath.startsWith(configDirPath)) {
            val graphic = tab.graphic as? HBox
            val label = graphic?.children?.find { it is Label } as? Label
            label?.textFill = Color.RED
        }

        // No listeners to remove, so return an empty cleanup function
        return {}
    }
}
