package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.geometry.Rectangle2D
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.stage.Screen
import javafx.stage.Stage

class WindowControls(
    private val stage: Stage
) : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("WindowControls")
    }

    private var previousBounds: Rectangle2D? = null
    private var maximizeButton: IconButton

    init {
        setupWindowControls()

        val minimizeButton = createMinimizeButton()
        maximizeButton = createMaximizeButton()
        val closeButton = createCloseButton()

        children.addAll(minimizeButton, maximizeButton, closeButton)
        logger.info("Window controls initialized")
    }

    private fun setupWindowControls() {
        styleClass.add("window-controls")
    }

    private fun createMinimizeButton(): IconButton {
        return IconButton {
            styleClass.addAll("window-control-button", "minimize-icon")
            tooltip = Tooltip("Minimize")
            setOnAction {
                stage.isIconified = true
            }
        }
    }

    private fun createMaximizeButton(): IconButton {
        return IconButton {
            updateMaximizeButtonStyle()
            tooltip = Tooltip("Maximize/Restore")
            setOnAction {
                toggleMaximize()
                updateMaximizeButtonStyle()
            }
        }
    }

    private fun IconButton.updateMaximizeButtonStyle() {
        styleClass.removeAll("maximize-icon", "restore-icon")
        styleClass.addAll("window-control-button", if (isStageMaximized()) "restore-icon" else "maximize-icon")
    }

    private fun createCloseButton(): IconButton {
        return IconButton {
            styleClass.addAll("window-control-button", "close-icon")
            tooltip = Tooltip("Close")
            setOnAction {
                stage.hide()
                logger.info("Window hidden via window controls close button")
            }
        }
    }

    private fun isStageMaximized(): Boolean {
        val screenBounds = Screen.getPrimary().visualBounds
        return stage.x == screenBounds.minX &&
                stage.y == screenBounds.minY &&
                stage.width == screenBounds.width &&
                stage.height == screenBounds.height
    }

    fun toggleMaximize() {
        val screenBounds = Screen.getPrimary().visualBounds
        if (isStageMaximized()) {
            previousBounds?.let {
                stage.x = it.minX
                stage.y = it.minY
                stage.width = it.width
                stage.height = it.height
            }
            logger.debug("Window restored from maximized state")
        } else {
            previousBounds = Rectangle2D(stage.x, stage.y, stage.width, stage.height)
            stage.x = screenBounds.minX
            stage.y = screenBounds.minY
            stage.width = screenBounds.width
            stage.height = screenBounds.height
            logger.debug("Window maximized to visual bounds")
        }
    }
}