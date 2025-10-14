package io.github.frostzie.datapackide.modules.bars.top

import net.minecraft.util.Util
import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.events.MenuControlsVisibilityChanged
import io.github.frostzie.datapackide.screen.MainApplication
import io.github.frostzie.datapackide.screen.elements.bars.top.TopBarView
import javafx.geometry.Rectangle2D
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.Screen
import javafx.stage.Stage
import java.net.URI

//TODO: Dragging not fully finished!
class TopBarModule(private val stage: Stage?, private val topBarView: TopBarView?) {

    private var previousBounds: Rectangle2D? = null
    private var isMaximized: Boolean = false

    private var dragStartX = 0.0
    private var dragStartY = 0.0
    private var dragStartStageX = 0.0
    private var dragStartStageY = 0.0
    private var isDraggingFromMaximized = false

    init {
        installDragHandlers()
    }

    private fun installDragHandlers() {
        topBarView?.let { view ->
            view.setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                    if (view.isOverDraggableArea(event)) {
                        toggleMaximize()
                        event.consume()
                    }
                }
            }

            view.setOnMousePressed { event ->
                if (event.button == MouseButton.PRIMARY && view.isOverDraggableArea(event)) {
                    dragStartX = event.screenX
                    dragStartY = event.screenY
                    dragStartStageX = stage?.x ?: 0.0
                    dragStartStageY = stage?.y ?: 0.0
                    isDraggingFromMaximized = isMaximized
                    event.consume()
                }
            }

            view.setOnMouseDragged { event ->
                if (event.button == MouseButton.PRIMARY && view.isOverDraggableArea(event)) {
                    if (isDraggingFromMaximized) {
                        handleDragFromMaximized(event)
                        isDraggingFromMaximized = false
                    } else {
                        stage?.x = dragStartStageX + (event.screenX - dragStartX)
                        stage?.y = dragStartStageY + (event.screenY - dragStartY)
                    }
                    event.consume()
                }
            }
        }
    }

    private fun handleDragFromMaximized(event: MouseEvent) {
        stage?.let { stg ->
            val restoredWidth = previousBounds?.width ?: stg.width
            val mouseXRatio = (event.screenX - stg.x) / stg.width

            stg.width = previousBounds?.width ?: stg.width
            stg.height = previousBounds?.height ?: stg.height
            isMaximized = false
            EventBus.post(MainWindowMaximizedStateChanged(false))

            val newX = event.screenX - (restoredWidth * mouseXRatio)
            val newY = event.screenY - (event.y)

            stg.x = newX
            stg.y = newY

            dragStartStageX = stg.x
            dragStartStageY = stg.y
            dragStartX = event.screenX
            dragStartY = event.screenY
        }
    }

    fun minimize() {
        stage?.isIconified = true
    }

    fun maximize() {
        stage?.let {
            val screen = Screen.getScreensForRectangle(it.x, it.y, it.width, it.height).firstOrNull() ?: Screen.getPrimary()
            val visualBounds = screen.visualBounds

            previousBounds = Rectangle2D(it.x, it.y, it.width, it.height)
            it.x = visualBounds.minX
            it.y = visualBounds.minY
            it.width = visualBounds.width
            it.height = visualBounds.height

            isMaximized = true
            EventBus.post(MainWindowMaximizedStateChanged(true))
        }
    }

    fun restore() {
        stage?.let { stg ->
            previousBounds?.let {
                stg.x = it.minX
                stg.y = it.minY
                stg.width = it.width
                stg.height = it.height
            }
            isMaximized = false
            EventBus.post(MainWindowMaximizedStateChanged(false))
        }
    }

    fun toggleMaximize() {
        if (isMaximized) {
            restore()
        } else {
            maximize()
        }
    }

    fun hideWindow() {
        MainApplication.hideMainWindow()
    }

    /**
     * Toggles the visibility of the menu bar.
     */
    private var isMenuControlsVisible = true

    fun toggleMenuControls() {
        isMenuControlsVisible = !isMenuControlsVisible
        EventBus.post(MenuControlsVisibilityChanged(isMenuControlsVisible))
    }

    fun aboutModLink() {
        Util.getOperatingSystem().open(URI("https://github.com/Frostzie/DataPack-IDE"))
    }

    fun discordLink() {
        Util.getOperatingSystem().open(URI("https://discord.gg/qZ885qTvkx"))
    }

    fun reloadDatapacks() {
        ReloadDataPacksCommand.executeCommandButton()
    }
}