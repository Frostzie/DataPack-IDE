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
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.kordamp.ikonli.feather.Feather
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
    private val spacer: Region

    init {
        HBox.setHgrow(this, Priority.ALWAYS)
        // Remove default toolbar padding to prevent overflow
        style = "-fx-padding: 0;"

        spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
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

        logger.debug("Window controls initialized")
    }

    /**
     * Check if a mouse event is over a draggable area (spacer or empty toolbar space)
     * This method is used by DragForwarding utility.
     */
    fun isOverDraggableArea(event: MouseEvent): Boolean {
        val target = event.target

        if (target == spacer || (target as? Region)?.styleClass?.contains("title-spacer") == true) {
            return true
        }

        if (target == this) {
            val localPoint = sceneToLocal(event.sceneX, event.sceneY)
            for (item in items) {
                if (item is Region && item != spacer) {
                    val bounds = item.boundsInParent
                    if (bounds.contains(localPoint)) {
                        return false
                    }
                }
            }
            return true
        }

        return false
    }

    private fun createHideMenuButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2OutlinedMZ.REORDER).apply {
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
            graphic = FontIcon(Feather.PLAY).apply {
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
            styleClass.addAll(Styles.FLAT)
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
            styleClass.addAll(Styles.FLAT)
            setOnAction {
                EventBus.post(MainWindowMinimize())
            }
        }
    }

    private fun createMaximizeButton(): IconButton {
        return IconButton {
            updateMaximizeButtonStyle()
            tooltip = Tooltip("Maximize/Restore")
            styleClass.addAll(Styles.FLAT)
            setOnAction {
                if (isMaximized) {
                    EventBus.post(MainWindowRestore())
                } else {
                    EventBus.post(MainWindowMaximize())
                }
            }
        }
    }

    private fun Button.updateMaximizeButtonStyle() {
        graphic = FontIcon(if (isMaximized) Material2AL.FILTER_NONE else Material2AL.CROP_SQUARE).apply {
            iconSize = 20
        }
    }

    private fun createCloseButton(): Button {
        return Button().apply {
            graphic = FontIcon(Material2AL.CLOSE).apply {
                iconSize = 20
            }
            tooltip = Tooltip("Close")
            styleClass.addAll(Styles.FLAT, Styles.DANGER)
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