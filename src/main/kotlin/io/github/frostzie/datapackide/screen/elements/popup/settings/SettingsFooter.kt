package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.SettingSave
import io.github.frostzie.datapackide.events.SettingsWindowCloseSave
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class SettingsFooter : HBox() {
    init {
        styleClass.add("button-section")

        val spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
        }

        val applyButton = Button("Apply").apply {
            styleClass.add("apply-button")
            setOnAction { EventBus.post(SettingSave()) }
        }

        val closeButton = Button("Close").apply {
            styleClass.add("close-button")
            setOnAction { EventBus.post(SettingsWindowCloseSave()) }
        }

        children.addAll(spacer, applyButton, closeButton)
    }
}