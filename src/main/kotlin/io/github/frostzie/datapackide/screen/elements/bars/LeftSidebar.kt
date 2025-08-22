package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.DirectorySelectedEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser

class LeftSidebar : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("LeftSidebar")
    }

    init {
        setupSidebar()
        createButtons()
    }

    private fun setupSidebar() {
        styleClass.add("left-sidebar")
        CSSManager.applyToComponent(stylesheets, "LeftBar")
        logger.info("Left sidebar initialized")
    }

    private fun createButtons() {
        val buttons = listOf(
            SidebarButton("folder",   "File Explorer") { onFileExplorer() },
            SidebarButton("search",   "Search")        { onSearch() },
            SidebarButton("play",      "Run")           { onRun() },
            SidebarButton("settings", "Settings") { onSettings() }
        )
        children.addAll(buttons)
    }

    private fun onSearch() {
        logger.info("Search button clicked")
    }

    private fun onFileExplorer() {
        logger.info("File Explorer button clicked")

        val directoryChooser = DirectoryChooser().apply {
            title = "Select Directory"
            try {
                initialDirectory = java.io.File(System.getProperty("user.home")) // TODO: change to .mc or saves folder
            } catch (e: Exception) {
                logger.warn("Could not set initial directory", e)
            }
        }

        val selectedDirectory = directoryChooser.showDialog(scene.window)
        if (selectedDirectory != null) {
            logger.info("Directory selected: ${selectedDirectory.absolutePath}")
            EventBus.post(DirectorySelectedEvent(selectedDirectory.toPath()))
        }
    }

    private fun onRun() {
        logger.info("Run button clicked")
    }

    private fun onSettings() {
        logger.info("Settings button clicked")
    }

    private inner class SidebarButton(
        iconName: String,
        tooltipText: String,
        private val action: () -> Unit
    ) : Button() {
        private val iconSize = 24.0

        init {
            tooltip = Tooltip(tooltipText)
            setOnAction { action() }
            styleClass.add("sidebar-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY

        val iconPath = "/assets/datapack-ide/themes/icon/$iconName.png"
        val iconStream = javaClass.getResourceAsStream(iconPath)
            ?: throw IllegalArgumentException("Icon not found: $iconPath")

        val imageView = ImageView(Image(iconStream)).apply {
            isPreserveRatio = true
            fitWidth = iconSize
            fitHeight = iconSize
            styleClass.add("sidebar-icon")
            }

        graphic = imageView
        }
    }

}