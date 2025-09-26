package io.github.frostzie.datapackide.eventsOLD.handlersOLD

import io.github.frostzie.datapackide.config.AssetsConfig
import io.github.frostzie.datapackide.eventsOLD.DirectorySelectedEvent
import io.github.frostzie.datapackide.eventsOLD.EventBusOLD
import io.github.frostzie.datapackide.eventsOLD.UIAction
import io.github.frostzie.datapackide.eventsOLD.UIActionEvent
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File

@Deprecated("Replacing with newer system")
/**
 * Handles UI-related actions such as window controls, opening dialogs, and toggling UI elements.
 */
class UIActionHandler(private val parentStage: Stage?) {
    companion object {
        private val logger = LoggerProvider.getLogger("UIActionHandler")
    }

    fun initialize() {
        EventBusOLD.register<UIActionEvent> { event ->
            logger.debug("Handling UI action: {}", event.action)
            handleUIAction(event)
        }
        logger.info("UIActionHandler initialized")
    }

    private fun handleUIAction(event: UIActionEvent) {
        when (event.action) {
            UIAction.TOGGLE_WINDOW -> toggleWindow()
            UIAction.OPEN_DIRECTORY_CHOOSER -> openDirectoryChooser()
            UIAction.TOGGLE_SEARCH -> toggleSearch()
            UIAction.SHOW_ABOUT -> showAbout()
            UIAction.RELOAD_STYLES -> reloadStyles()
            UIAction.RESET_STYLES_TO_DEFAULT -> resetStylesToDefault()
            UIAction.SAVE_SETTINGS -> {} // This action is handled by SettingsManager
            UIAction.SHOW_SETTINGS -> logger.info("Open Settings")
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
            EventBusOLD.post(DirectorySelectedEvent(selectedDirectory.toPath()))
        }
    }

    private fun toggleSearch() {
        logger.info("Search toggle requested")
        // TODO: Implement search functionality
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