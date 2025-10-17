package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.SettingsWindowCloseSave
import io.github.frostzie.datapackide.events.SettingsWindowDragged
import io.github.frostzie.datapackide.events.SettingsWindowDragStarted
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class SettingsHeader : HBox() {
    init {
        styleClass.add("title-section")

        val titleLabel = Label("Settings").apply {
            styleClass.add("title-label")
        }

        val spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
        }

        val closeButton = Button("✕").apply {
            styleClass.add("title-close-button")
            setOnAction { EventBus.post(SettingsWindowCloseSave()) }
        }

        children.addAll(titleLabel, spacer, closeButton)

        setOnMousePressed { event ->
            EventBus.post(SettingsWindowDragStarted(event.screenX, event.screenY))
        }

        setOnMouseDragged { event ->
            EventBus.post(SettingsWindowDragged(event.screenX, event.screenY))
        }
    }
}