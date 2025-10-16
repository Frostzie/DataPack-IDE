package io.github.frostzie.datapackide.utils.dev

import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import javafx.beans.property.BooleanProperty
import javafx.scene.layout.Pane

//TODO: make debug lines not interfere with actual element sizes
object DebugManager {
    fun initialize(root: Pane) {
        applyStyleOnToggle(root, AdvancedConfig.debugLayoutBounds, "debug-layout")
        applyStyleOnToggle(root, AdvancedConfig.debugResizeHandles, "debug-resize-handles")
    }

    private fun applyStyleOnToggle(pane: Pane, property: BooleanProperty, styleClass: String) {
        property.addListener { _, _, newValue ->
            if (newValue) {
                pane.styleClass.add(styleClass)
            } else {
                pane.styleClass.remove(styleClass)
            }
        }
        if (property.get()) {
            pane.styleClass.add(styleClass)
        }
    }
}