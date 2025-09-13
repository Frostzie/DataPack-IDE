package io.github.frostzie.datapackide.events.handlers

import io.github.frostzie.datapackide.config.AssetsConfig
import io.github.frostzie.datapackide.events.DirectorySelectedEvent
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.WindowStateEvent
import io.github.frostzie.datapackide.events.UIAction
import io.github.frostzie.datapackide.events.UIActionEvent
import io.github.frostzie.datapackide.settings.SettingsController
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.geometry.Rectangle2D
import javafx.stage.DirectoryChooser
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent
import java.io.File

/**
 * Handles UI-related actions such as window controls, opening dialogs, and toggling UI elements.
 */
class UIActionHandler(private val parentStage: Stage?) {
    companion object {
        private val logger = LoggerProvider.getLogger("UIActionHandler")
    }

    private var previousBounds: Rectangle2D? = null

    fun initialize() {
        EventBus.register<UIActionEvent> { event ->
            logger.debug("Handling UI action: ${event.action}")
            handleUIAction(event)
        }
        logger.info("UIActionHandler initialized")
    }

    private fun handleUIAction(event: UIActionEvent) {
        when (event.action) {
            UIAction.MINIMIZE_WINDOW -> parentStage?.isIconified = true
            UIAction.MAXIMIZE_WINDOW -> maximizeWindow()
            UIAction.RESTORE_WINDOW -> restoreWindow()
            UIAction.TOGGLE_WINDOW_MODES -> toggleMaximize()
            UIAction.REQUEST_WINDOW_CLOSE -> fireWindowCloseRequest()
            UIAction.TOGGLE_WINDOW -> toggleWindow()
            UIAction.OPEN_DIRECTORY_CHOOSER -> openDirectoryChooser()
            UIAction.TOGGLE_SEARCH -> toggleSearch()
            UIAction.SHOW_SETTINGS -> showPreferences()
            UIAction.SHOW_ABOUT -> showAbout()
            UIAction.RELOAD_STYLES -> reloadStyles()
            UIAction.RESET_STYLES_TO_DEFAULT -> resetStylesToDefault()
            UIAction.SAVE_SETTINGS -> {} // This action is handled by SettingsManager
        }
    }

    private fun isStageMaximized(): Boolean {
        if (parentStage == null) return false
        val screenBounds = Screen.getPrimary().visualBounds
        return parentStage.x == screenBounds.minX &&
                parentStage.y == screenBounds.minY &&
                parentStage.width == screenBounds.width &&
                parentStage.height == screenBounds.height
    }

    private fun toggleMaximize() {
        if (isStageMaximized()) {
            restoreWindow()
        } else {
            maximizeWindow()
        }
    }

    private fun maximizeWindow() {
        parentStage?.let { stage ->
            val screenBounds = Screen.getPrimary().visualBounds
            previousBounds = Rectangle2D(stage.x, stage.y, stage.width, stage.height)
            stage.x = screenBounds.minX
            stage.y = screenBounds.minY
            stage.width = screenBounds.width
            stage.height = screenBounds.height
            logger.debug("Window maximized")
            EventBus.post(WindowStateEvent(isVisible = true, isMaximized = true))
        }
    }

    private fun restoreWindow() {
        parentStage?.let { stage ->
            previousBounds?.let {
                stage.x = it.minX
                stage.y = it.minY
                stage.width = it.width
                stage.height = it.height
            }
            logger.debug("Window restored from maximized state")
            EventBus.post(WindowStateEvent(isVisible = true, isMaximized = false))
        }
    }

    private fun fireWindowCloseRequest() {
        parentStage?.let {
            logger.info("Window close requested.")
            EventBus.post(WindowStateEvent(isVisible = false))
            it.fireEvent(
                WindowEvent(
                    it,
                    WindowEvent.WINDOW_CLOSE_REQUEST
                )
            )
        }
    }

    private fun toggleWindow() {
        parentStage?.let { stage ->
            if (stage.isShowing) {
                stage.hide()
            } else {
                stage.show()
                stage.toFront()
            }
        }
    }

    private fun openDirectoryChooser() {
        val directoryChooser = DirectoryChooser().apply {
            title = "Select Directory"
            try {
                val os = System.getProperty("os.name").lowercase()
                val minecraftPath = when {
                    os.contains("win") -> System.getenv("APPDATA")?.let { File(it, ".minecraft") }
                    os.contains("mac") -> File(System.getProperty("user.home"), "Library/Application Support/minecraft")
                    else -> File(System.getProperty("user.home"), ".minecraft")
                }

                if (minecraftPath != null && minecraftPath.exists()) {
                    initialDirectory = minecraftPath
                } else {
                    initialDirectory = File(System.getProperty("user.home"))
                }
            } catch (e: Exception) {
                logger.warn("Could not set initial directory", e)
            }
        }

        val selectedDirectory = directoryChooser.showDialog(parentStage)
        if (selectedDirectory != null) {
            EventBus.post(DirectorySelectedEvent(selectedDirectory.toPath()))
        }
    }

    private fun toggleSearch() {
        logger.info("Search toggle requested")
        // TODO: Implement search functionality
    }

    private fun showPreferences() {
        val settingsController = SettingsController(parentStage)
        settingsController.show()
    }

    private fun showAbout() {
        logger.info("About dialog requested")
        // TODO: Implement about dialog
    }

    private fun reloadStyles() {
        val scenesToReload = listOfNotNull(parentStage?.scene)
        CSSManager.reloadAllStyles(*scenesToReload.toTypedArray())
    }

    private fun resetStylesToDefault() {
        AssetsConfig.forceTransferAllAssets()
        val scenesToReload = listOfNotNull(parentStage?.scene)
        CSSManager.reloadAllStyles(*scenesToReload.toTypedArray())
    }
}