package io.github.frostzie.datapackide.screen.elements.bars.top

import atlantafx.base.theme.Styles
import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.feather.Feather
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import io.github.frostzie.datapackide.utils.UIConstants
import org.kordamp.ikonli.material2.Material2MZ
import org.kordamp.ikonli.material2.Material2OutlinedMZ

class TopBarView(private val toolBarMenu: ToolBarMenu) : ToolBar() {

    private val maximizeButton: Button
    private val spacer: Region

    init {
        HBox.setHgrow(this, Priority.ALWAYS)
        prefHeight = UIConstants.TOP_BAR_HEIGHT
        minHeight = UIConstants.TOP_BAR_HEIGHT
        maxHeight = UIConstants.TOP_BAR_HEIGHT
        styleClass.add("top-bar-view")

        spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
        }

        val hideMenuButton = createTopBarButton(
            Material2OutlinedMZ.REORDER,
            "Toggle Menu Bar",
            Styles.BUTTON_OUTLINED
        ) {
            toolBarMenu.show()
        }

        val runDataPackButton = createTopBarButton(
            Feather.PLAY,
            "Reload Datapack"
        ) {
            EventBus.post(SaveAllFiles())
            EventBus.post(ReloadDatapack())
        }

        val settingsButton = createTopBarButton(
            Material2MZ.SETTINGS,
            "Settings"
        ) {
            EventBus.post(SettingsWindowOpen())
        }

        val minimizeButton = createTopBarButton(
            Material2OutlinedMZ.MINIMIZE,
            "Minimize"
        ) {
            EventBus.post(MainWindowMinimize())
        }

        maximizeButton = createTopBarButton(
            Material2AL.CROP_SQUARE,
            "Maximize/Restore"
        ) {
            EventBus.post(MainWindowToggleMaximize())
        }

        val closeButton = createTopBarButton(
            Material2AL.CLOSE,
            "Close",
            Styles.DANGER
        ) {
            EventBus.post(MainWindowClose())
        }

        updateMaximizeButtonIcon(false)

        items.addAll(
            hideMenuButton,
            spacer,
            runDataPackButton,
            settingsButton,
            minimizeButton,
            maximizeButton,
            closeButton
        )
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

    private fun createTopBarButton(icon: Ikon, tooltipText: String, vararg styleClasses: String, action: () -> Unit): Button {
        return Button().apply {
            graphic = FontIcon(icon)
            tooltip = Tooltip(tooltipText)
            styleClass.addAll(Styles.FLAT, *styleClasses)

            val size = UIConstants.TOP_BAR_BUTTON_SIZE
            prefWidth = size
            minWidth = size
            maxWidth = size
            prefHeight = size
            minHeight = size
            maxHeight = size

            setOnAction { action() }
        }
    }

    private fun updateMaximizeButtonIcon(maximized: Boolean) {
        maximizeButton.graphic = FontIcon(if (maximized) Material2AL.FILTER_NONE else Material2AL.CROP_SQUARE)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWindowStateChanged(event: MainWindowMaximizedStateChanged) {
        Platform.runLater {
            updateMaximizeButtonIcon(event.isMaximized)
        }
    }
}