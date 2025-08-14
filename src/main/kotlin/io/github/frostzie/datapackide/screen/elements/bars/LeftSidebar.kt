package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Tooltip
import javafx.scene.shape.FillRule
import javafx.scene.shape.SVGPath
import javafx.scene.layout.VBox
import kotlin.math.min

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
        stylesheets.add(javaClass.getResource("/assets/datapack-ide/themes/sidebar.css")?.toExternalForm())
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

            val svgPath = SVGPath().apply {
                content = loadSvgPath(iconName)
                fillRule = FillRule.NON_ZERO
                styleClass.addAll("svg-icon", "icon-$iconName")
            }

            val bounds = svgPath.layoutBounds
            if (bounds.width > 0 && bounds.height > 0) {
                val scale = min(iconSize / bounds.width, iconSize / bounds.height)
                svgPath.scaleX = scale
                svgPath.scaleY = scale
            }

            graphic = svgPath
        }
    }

    private fun loadSvgPath(iconName: String): String {
        val path = "/assets/datapack-ide/themes/icon/$iconName.svg"
        val stream = javaClass.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Icon not found: $path")
        val xml = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val regex = Regex("""<path[^>]*\sd=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val match = regex.find(xml)
            ?: throw IllegalStateException("No <path d=\"...\"> found in $path")
        return match.groupValues[1]
    }
}
