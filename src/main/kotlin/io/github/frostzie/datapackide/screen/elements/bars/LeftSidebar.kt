package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.eventsOLD.EventBusOLD
import io.github.frostzie.datapackide.eventsOLD.UIActionEvent
import io.github.frostzie.datapackide.eventsOLD.UIAction
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

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
        logger.info("Left sidebar initialized with event system")
    }

    private fun createButtons() {
        val buttons = listOf(
            SidebarButton("folder",   "File Explorer") { onFileExplorer() },
            SidebarButton("search",   "Search")        { onSearch() }
        )
        children.addAll(buttons)
    }

    //TODO: does nothing yet! (was used for testing event fires in log)
    private fun onSearch() {
        logger.info("Search button clicked")
        EventBusOLD.post(UIActionEvent(UIAction.TOGGLE_SEARCH))
    }

    private fun onFileExplorer() {
        logger.info("File Explorer button clicked")
        EventBusOLD.post(UIActionEvent(UIAction.OPEN_DIRECTORY_CHOOSER))
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