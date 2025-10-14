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
import javafx.stage.Screen
import javafx.stage.Stage
import java.net.URI

//TODO: Dragging not fully finished!
class TopBarModule(private val stage: Stage?, private val topBarView: TopBarView?) {

    private var previousBounds: Rectangle2D? = null
    private var isMaximized: Boolean = false

    init {
        doubleClickMaximize()
    }

    private fun doubleClickMaximize() {
        topBarView?.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                toggleMaximize()
            }
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