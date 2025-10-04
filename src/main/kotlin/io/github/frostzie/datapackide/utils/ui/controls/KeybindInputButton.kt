package io.github.frostzie.datapackide.utils.ui.controls

import io.github.frostzie.datapackide.settings.KeyCombination
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Button
import javafx.scene.input.KeyCode

/**
 * A custom button that listens for and captures a [KeyCombination].
 * The captured combination is stored in the [keybindProperty].
 */
class KeybindInputButton : Button() {

    val keybindProperty = SimpleObjectProperty<KeyCombination>()

    private var isListening = false

    init {
        styleClass.add("keybind-input-button")

        keybindProperty.addListener { _, _, newKeybind ->
            text = newKeybind?.toString() ?: "None"
        }

        text = keybindProperty.get()?.toString() ?: "None"

        setOnAction { event ->
            if (!isListening) {
                startListening()
            }
        }

        setOnKeyPressed { event ->
            if (isListening) {
                event.consume()

                if (event.code == KeyCode.ESCAPE) {
                    stopListening(false)
                    return@setOnKeyPressed
                }

                if (event.code.isModifierKey || event.code == KeyCode.UNDEFINED || event.code == KeyCode.CAPS) {
                    return@setOnKeyPressed
                }

                val newKeybind = KeyCombination.fromEvent(event)
                keybindProperty.set(newKeybind)
                stopListening(true)
            }
        }

        focusedProperty().addListener { _, _, hasFocus ->
            if (!hasFocus && isListening) {
                stopListening(false)
            }
        }
    }

    private fun startListening() {
        isListening = true
        text = "Press any key..."
        styleClass.add("listening")
    }

    private fun stopListening(wasCompleted: Boolean) {
        isListening = false

        if (!wasCompleted) {
            text = keybindProperty.get()?.toString() ?: "None"
        }
        styleClass.remove("listening")
    }
}