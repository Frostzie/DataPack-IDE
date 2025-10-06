package io.github.frostzie.datapackide.modules.bars.top

import net.minecraft.util.Util
import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.events.MenuControlsVisibilityChanged
import io.github.frostzie.datapackide.screen.MainApplication
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import java.net.URI

class TopBarModule(private val stage: Stage?) {

    /**
     * Windows resizing function.
     */
    private var previousBounds: Rectangle2D? = null

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
            EventBus.post(MainWindowMaximizedStateChanged(true))
        }
    }

    fun restore() {
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

    /**
     * Menu button actions
     */
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