package io.github.frostzie.datapackide.modules.bars.top

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.eventsOLD.MenuBarVisibilityChanged
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent

class TopBarModule(private val stage: Stage?) {

    /**
     * Windows resizing function.
     */
    private var previousBounds: Rectangle2D? = null
    private val isMaximized: Boolean
        get() {
            if (stage == null) return false
            for (screen in Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)) {
                val visualBounds = screen.visualBounds
                if (stage.x == visualBounds.minX &&
                    stage.y == visualBounds.minY &&
                    stage.width == visualBounds.width &&
                    stage.height == visualBounds.height) {
                    return true
                }
            }
            return false
        }

    fun minimize() {
        stage?.isIconified = true
    }

    fun toggleMaximize() {
        if (isMaximized) {
            restore()
        } else {
            maximize()
        }
    }

    fun close() {
        stage?.fireEvent(WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))
    }

    private fun maximize() {
        stage?.let {
            val screen = Screen.getScreensForRectangle(it.x, it.y, it.width, it.height).firstOrNull() ?: Screen.getPrimary()
            val visualBounds = screen.visualBounds
            previousBounds = Rectangle2D(it.x, it.y, it.width, it.height)
            it.x = visualBounds.minX
            it.y = visualBounds.minY
            it.width = visualBounds.width
            it.height = visualBounds.height
            EventBus.post(MainWindowMaximizedStateChanged(true))
        }
    }

    private fun restore() {
        stage?.let { stage ->
            previousBounds?.let {
                stage.x = it.minX
                stage.y = it.minY
                stage.width = it.width
                stage.height = it.height
            }
            EventBus.post(MainWindowMaximizedStateChanged(false))
        }
    }

    /**
     * Toggles the visibility of the menu bar.
     */
    private var isMenuBarVisible = true

    fun toggleMenuBar() {
        isMenuBarVisible = !isMenuBarVisible
        EventBus.post(MenuBarVisibilityChanged(isMenuBarVisible))
    }
}