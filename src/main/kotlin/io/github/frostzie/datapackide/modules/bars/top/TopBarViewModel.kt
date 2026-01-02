package io.github.frostzie.datapackide.modules.bars.top

import io.github.frostzie.datapackide.loader.minecraft.ReloadDataPacksCommand
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.screen.MainApplication
import io.github.frostzie.datapackide.utils.file.DirectoryChooseUtils
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import net.minecraft.Util

class TopBarViewModel(private val stage: Stage?) {

    private var previousBounds: Rectangle2D? = null
    var isMaximized: Boolean = false

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
        previousBounds?.let { bounds ->
            stage?.let { stg ->
                stg.x = bounds.minX
                stg.y = bounds.minY
                stg.width = bounds.width
                stg.height = bounds.height

                isMaximized = false
                EventBus.post(MainWindowMaximizedStateChanged(false))
            }
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

    fun reloadDatapacks() {
        ReloadDataPacksCommand.executeCommandButton()
    }

    fun openDatapackFolder() {
        DirectoryChooseUtils.getDatapackPath()?.let {
            Util.getPlatform().openFile(it.toFile())
        }
    }
}