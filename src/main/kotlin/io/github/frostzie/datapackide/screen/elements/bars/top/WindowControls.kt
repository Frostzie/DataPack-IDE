package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.events.MainWindowRestore
import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowClose
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowMinimize
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import javafx.application.Platform
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox

class WindowControls : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("WindowControls")
    }

    private var isMaximized = false
    private var maximizeButton: IconButton
    private var minimizeButton: IconButton
    private var closeButton: IconButton

    init {
        setupWindowControls()

        minimizeButton = createMinimizeButton()
        maximizeButton = createMaximizeButton()
        closeButton = createCloseButton()

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
                EventBus.post(MainWindowMinimize())
            }
        }
    }

    private fun createMaximizeButton(): IconButton {
        return IconButton {
            updateMaximizeButtonStyle()
            tooltip = Tooltip("Maximize/Restore")
            setOnAction {
                if (isMaximized) {
                    EventBus.post(MainWindowRestore())
                } else {
                    EventBus.post(MainWindowMaximize())
                }
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
                EventBus.post(MainWindowClose())
            }
        }
    }

    @SubscribeEvent
    fun onWindowStateChanged(event: MainWindowMaximizedStateChanged) {
        if (isMaximized != event.isMaximized) {
            isMaximized = event.isMaximized
            Platform.runLater {
                maximizeButton.updateMaximizeButtonStyle()
            }
        }
    }
}