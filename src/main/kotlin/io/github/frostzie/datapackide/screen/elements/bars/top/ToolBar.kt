package io.github.frostzie.datapackide.screen.elements.bars.top

import atlantafx.base.theme.Styles
import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ
import org.kordamp.ikonli.material2.Material2OutlinedMZ

class ToolBar : ToolBar() {

    companion object {
        private val logger = LoggerProvider.getLogger("WindowControls")
    }

    var isMaximized = false
    private var maximizeButton: IconButton
    private var minimizeButton: Button
    private var closeButton: Button

    init {
        // Make this HBox grow to fill available space
        HBox.setHgrow(this, Priority.ALWAYS)
        // Remove default toolbar padding to prevent overflow.
        style = "-fx-padding: 0;"

        val spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer") // Add class for double-click-to-maximize detection
        }

        minimizeButton = createMinimizeButton()
        maximizeButton = createMaximizeButton()
        closeButton = createCloseButton()

        items.addAll(
            createHideMenuButton(),
            spacer,
            createRunDataPackButton(),
            createSettingsButton(),
            minimizeButton,
            maximizeButton,
            closeButton
        )

        // TODO: Fix window dragging and resizing. The ToolBar consumes mouse events,
        //  preventing the ResizeHandler from working correctly in this area.
        logger.debug("Window controls initialized")
    }

    private fun createHideMenuButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2OutlinedMZ.VERTICAL_ALIGN_BOTTOM).apply {
                iconSize = 20
            }
            tooltip = Tooltip("Toggle Menu Bar")
            styleClass.addAll(Styles.FLAT, Styles.BUTTON_OUTLINED)
            setOnAction {
                //EventBus.post(ToggleMenuControls())
            }
        }
    }

    private fun createRunDataPackButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2OutlinedMZ.PLAY_ARROW).apply {
                iconSize = 20
            }
            tooltip = Tooltip("Reload Datapack")
            styleClass.addAll(Styles.FLAT)
            setOnAction {
                EventBus.post(ReloadDatapack())
            }
        }
    }

    private fun createSettingsButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2MZ.SETTINGS).apply {
                iconSize = 20
            }
            tooltip = Tooltip("Settings")
            styleClass.addAll( Styles.FLAT)
            setOnAction {
                EventBus.post(SettingsWindowOpen())
            }
        }
    }

    private fun createMinimizeButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2OutlinedMZ.MINIMIZE).apply {
                iconSize = 20
            }
            tooltip = Tooltip("Minimize")
            styleClass.addAll(Styles.BUTTON_ICON, Styles.SUCCESS, Styles.CENTER)
            setOnAction {
                EventBus.post(MainWindowMinimize())
            }
        }
    }

    private fun createMaximizeButton(): IconButton {
        return IconButton {
            style = "-fx-font-size: 16px;" // Set font size for the icon character
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

    private fun Button.updateMaximizeButtonStyle() { //TODO: Fix
        styleClass.removeAll("maximize-icon", "restore-icon")
        styleClass.addAll("window-control-button", if (isMaximized) "restore-icon" else "maximize-icon")
    }

    private fun createCloseButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2AL.CLOSE).apply {
                iconSize = 20
            }
            tooltip = Tooltip("Close")
            styleClass.addAll(Styles.BUTTON_ICON, Styles.SUCCESS, Styles.CENTER)
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