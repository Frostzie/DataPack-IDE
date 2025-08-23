package io.github.frostzie.datapackide.utils

import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region

/**
 * Utility class for making UI components resizable by dragging
 */
object ResizableUtils {
    private val logger = LoggerProvider.getLogger("ResizableUtils")

    private var isResizing = false
    private var startX = 0.0
    private var startWidth = 0.0

    /**
     * Makes a FileTree component horizontally resizable by dragging its right edge
     *
     * @param fileTreeContainer The container node to make resizable
     * @param minWidth Minimum width constraint (default: 150px)
     * @param maxWidth Maximum width constraint (default: 600px)
     * @param resizeZoneWidth Width of the resize zone on the right edge (default: 8px)
     */
    fun makeFileTreeResizable(
        fileTreeContainer: Region,
        minWidth: Double = 150.0,
        maxWidth: Double = 600.0,
        resizeZoneWidth: Double = 8.0
    ) {
        logger.info("Making FileTree resizable with bounds: min=$minWidth, max=$maxWidth")

        val initialWidth = fileTreeContainer.prefWidth
        fileTreeContainer.minWidth = initialWidth
        fileTreeContainer.prefWidth = initialWidth
        fileTreeContainer.maxWidth = initialWidth

        fileTreeContainer.setOnMouseMoved { event ->
            handleMouseMoved(event, fileTreeContainer, resizeZoneWidth)
        }

        fileTreeContainer.setOnMousePressed { event ->
            handleMousePressed(event, fileTreeContainer, resizeZoneWidth)
        }

        fileTreeContainer.setOnMouseDragged { event ->
            handleMouseDragged(event, fileTreeContainer, minWidth, maxWidth)
        }

        fileTreeContainer.setOnMouseReleased { event ->
            handleMouseReleased(event, fileTreeContainer)
        }

        fileTreeContainer.setOnMouseExited { event ->
            if (!isResizing) {
                fileTreeContainer.cursor = Cursor.DEFAULT
            }
        }

        logger.debug("FileTree resize handlers configured")

        // TODO: Load saved width from config file on initialization
        // TODO: Save current width to config when resize operation completes
    }

    private fun handleMouseMoved(event: MouseEvent, container: Region, resizeZoneWidth: Double) {
        val mouseX = event.x
        val containerWidth = container.width

        val inResizeZone = mouseX >= containerWidth - resizeZoneWidth && mouseX <= containerWidth

        if (inResizeZone && !isResizing) {
            container.cursor = Cursor.H_RESIZE
        } else if (!isResizing) {
            container.cursor = Cursor.DEFAULT
        }
    }

    private fun handleMousePressed(event: MouseEvent, container: Region, resizeZoneWidth: Double) {
        val mouseX = event.x
        val containerWidth = container.width

        val inResizeZone = mouseX >= containerWidth - resizeZoneWidth && mouseX <= containerWidth

        if (inResizeZone) {
            isResizing = true
            startX = event.sceneX
            startWidth = container.width
            container.cursor = Cursor.H_RESIZE
            event.consume()
            logger.debug("Started resizing FileTree from width: $startWidth")
        }
    }

    private fun handleMouseDragged(event: MouseEvent, container: Region, minWidth: Double, maxWidth: Double) {
        if (!isResizing) return

        val deltaX = event.sceneX - startX
        val newWidth = startWidth + deltaX

        val constrainedWidth = when {
            newWidth < minWidth -> minWidth
            newWidth > maxWidth -> maxWidth
            else -> newWidth
        }

        container.prefWidth = constrainedWidth
        container.minWidth = constrainedWidth
        container.maxWidth = constrainedWidth

        container.autosize()

        event.consume()
        logger.debug("Resizing FileTree to width: $constrainedWidth")
    }

    private fun handleMouseReleased(event: MouseEvent, container: Region) {
        if (isResizing) {
            isResizing = false
            container.cursor = Cursor.DEFAULT
            val finalWidth = container.width

            container.prefWidth = finalWidth
            container.minWidth = finalWidth
            container.maxWidth = finalWidth

            logger.info("Completed FileTree resize to width: $finalWidth")

            // TODO: Save the final width to config here

            event.consume()
        }
    }

    /**
     * Reset FileTree to default width
     *
     * @param fileTreeContainer The FileTree container to reset
     * @param defaultWidth The default width to reset to (default: 250px)
     */
    fun resetFileTreeWidth(fileTreeContainer: Region, defaultWidth: Double = 250.0) {
        fileTreeContainer.prefWidth = defaultWidth
        fileTreeContainer.minWidth = defaultWidth
        fileTreeContainer.maxWidth = defaultWidth
        fileTreeContainer.autosize()
        logger.info("Reset FileTree width to default: $defaultWidth")

        // TODO: Save reset width to config
    }

    /**
     * Get current resize state
     */
    fun isCurrentlyResizing(): Boolean = isResizing
}