package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.UIActionEvent
import io.github.frostzie.datapackide.events.UIAction
import io.github.frostzie.datapackide.events.WindowStateEvent
import javafx.application.Platform
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox

class WindowControls : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("WindowControls")
    }

    private var isMaximized = false
    private var maximizeButton: IconButton

    init {
        setupWindowControls()
        registerEventHandlers()

        val minimizeButton = createMinimizeButton()
        maximizeButton = createMaximizeButton()
        val closeButton = createCloseButton()

        children.addAll(minimizeButton, maximizeButton, closeButton)
        logger.debug("Window controls initialized")
    }

    private fun setupWindowControls() {
        styleClass.add("window-controls")
    }

    private fun createMinimizeButton(): IconButton {
        return IconButton {
            styleClass.addAll("window-control-button", "minimize-icon")
            tooltip = Tooltip("Minimize")
            setOnAction {
                EventBus.post(UIActionEvent(UIAction.MINIMIZE_WINDOW))
            }
        }
    }

    private fun createMaximizeButton(): IconButton {
        return IconButton {
            updateMaximizeButtonStyle()
            tooltip = Tooltip("Maximize/Restore")
            setOnAction {
                val newAction = if (isMaximized) UIAction.RESTORE_WINDOW else UIAction.MAXIMIZE_WINDOW
                EventBus.post(UIActionEvent(newAction))
            }
        }
    }

    private fun IconButton.updateMaximizeButtonStyle() {
        styleClass.removeAll("maximize-icon", "restore-icon")
        styleClass.addAll("window-control-button", if (isMaximized) "restore-icon" else "maximize-icon")
    }

    private fun createCloseButton(): IconButton {
        return IconButton {
            styleClass.addAll("window-control-button", "close-icon")
            tooltip = Tooltip("Close")
            setOnAction {
                EventBus.post(UIActionEvent(UIAction.REQUEST_WINDOW_CLOSE))
            }
        }
    }

    private fun registerEventHandlers() {
        EventBus.register<WindowStateEvent> { event ->
            if (isMaximized != event.isMaximized) {
                isMaximized = event.isMaximized
                Platform.runLater {
                    maximizeButton.updateMaximizeButtonStyle()
                }
            }
        }
    }
}