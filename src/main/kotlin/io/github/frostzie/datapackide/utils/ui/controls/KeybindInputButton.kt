package io.github.frostzie.datapackide.utils.ui.controls

import io.github.frostzie.datapackide.settings.KeyCombination
import javafx.scene.control.Button
import javafx.scene.input.KeyCode

/**
 * A custom button that listens for and captures a [KeyCombination].
 */
class KeybindInputButton(
    initialValue: KeyCombination,
    private val onKeybindChanged: (KeyCombination) -> Unit
) : Button(initialValue.toString()) {

    var currentKeybind: KeyCombination = initialValue
        set(value) {
            field = value
            text = value.toString()
        }

    private var isListening = false

    init {
        styleClass.add("keybind-input-button")

        setOnAction {
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
                currentKeybind = newKeybind
                onKeybindChanged(newKeybind)
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
            text = currentKeybind.toString()
        }
        styleClass.remove("listening")
    }
}