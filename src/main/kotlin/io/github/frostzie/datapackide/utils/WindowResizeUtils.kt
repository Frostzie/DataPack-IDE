package io.github.frostzie.datapackide.utils

import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Stage

/**
 * Utility class for making undecorated JavaFX windows resizable by dragging edges and corners
 */
object WindowResizeUtils {
    private val logger = LoggerProvider.getLogger("WindowResizeUtils")

    private var resizing = false
    private var startX = 0.0
    private var startY = 0.0
    private var startWidth = 0.0
    private var startHeight = 0.0
    private var resizeDirection = ResizeDirection.NONE

    private const val RESIZE_MARGIN = 8.0

    enum class ResizeDirection {
        NONE, NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
    }

    /**
     * Makes an undecorated stage resizable by adding mouse handlers to a root node
     *
     * @param stage The Stage to make resizable
     * @param rootNode The root node that will handle mouse events
     * @param minWidth Minimum window width (default: 400px)
     * @param minHeight Minimum window height (default: 300px)
     */
    fun makeStageResizable(
        stage: Stage,
        rootNode: Node,
        minWidth: Double = 400.0,
        minHeight: Double = 300.0
    ) {
        logger.info("Making stage resizable with bounds: min=${minWidth}x${minHeight}")

        stage.minWidth = minWidth
        stage.minHeight = minHeight

        rootNode.setOnMouseMoved { event ->
            handleMouseMoved(event, stage, rootNode)
        }

        rootNode.setOnMousePressed { event ->
            handleMousePressed(event, stage)
        }

        rootNode.setOnMouseDragged { event ->
            handleMouseDragged(event, stage)
        }

        rootNode.setOnMouseReleased { event ->
            handleMouseReleased(event, stage, rootNode)
        }

        logger.debug("Stage resize handlers configured")
    }

    private fun handleMouseMoved(event: MouseEvent, stage: Stage, rootNode: Node) {
        if (resizing) return

        var targetNode: Node? = event.target as? Node
        while (targetNode != null) {
            if (targetNode.styleClass.contains("file-tree-container")) {
                if (rootNode.cursor != Cursor.DEFAULT) rootNode.cursor = Cursor.DEFAULT
                return
            }
            targetNode = targetNode.parent
        }

        val direction = getResizeDirection(event, stage)
        val cursor = when (direction) {
            ResizeDirection.NORTH, ResizeDirection.SOUTH -> Cursor.V_RESIZE
            ResizeDirection.EAST, ResizeDirection.WEST -> Cursor.H_RESIZE
            ResizeDirection.NORTHEAST, ResizeDirection.SOUTHWEST -> Cursor.NE_RESIZE
            ResizeDirection.NORTHWEST, ResizeDirection.SOUTHEAST -> Cursor.NW_RESIZE
            else -> Cursor.DEFAULT
        }

        rootNode.cursor = cursor
    }

    private fun handleMousePressed(event: MouseEvent, stage: Stage) {
        val direction = getResizeDirection(event, stage)

        if (direction != ResizeDirection.NONE) {
            resizing = true
            resizeDirection = direction
            startX = event.screenX
            startY = event.screenY
            startWidth = stage.width
            startHeight = stage.height
            event.consume()
            logger.debug("Started resizing window from: ${startWidth}x${startHeight}, direction: $direction")
        }
    }

    private fun handleMouseDragged(event: MouseEvent, stage: Stage) {
        if (!resizing) return

        val deltaX = event.screenX - startX
        val deltaY = event.screenY - startY

        var newX = stage.x
        var newY = stage.y
        var newWidth = startWidth
        var newHeight = startHeight

        when (resizeDirection) {
            ResizeDirection.NORTH -> {
                newY = stage.y + deltaY
                newHeight = startHeight - deltaY
            }
            ResizeDirection.SOUTH -> {
                newHeight = startHeight + deltaY
            }
            ResizeDirection.EAST -> {
                newWidth = startWidth + deltaX
            }
            ResizeDirection.WEST -> {
                newX = stage.x + deltaX
                newWidth = startWidth - deltaX
            }
            ResizeDirection.NORTHEAST -> {
                newY = stage.y + deltaY
                newWidth = startWidth + deltaX
                newHeight = startHeight - deltaY
            }
            ResizeDirection.NORTHWEST -> {
                newX = stage.x + deltaX
                newY = stage.y + deltaY
                newWidth = startWidth - deltaX
                newHeight = startHeight - deltaY
            }
            ResizeDirection.SOUTHEAST -> {
                newWidth = startWidth + deltaX
                newHeight = startHeight + deltaY
            }
            ResizeDirection.SOUTHWEST -> {
                newX = stage.x + deltaX
                newWidth = startWidth - deltaX
                newHeight = startHeight + deltaY
            }
            else -> return
        }

        if (newWidth < stage.minWidth) {
            if (resizeDirection == ResizeDirection.WEST ||
                resizeDirection == ResizeDirection.NORTHWEST ||
                resizeDirection == ResizeDirection.SOUTHWEST) {
                newX = stage.x + (startWidth - stage.minWidth)
            }
            newWidth = stage.minWidth
        }

        if (newHeight < stage.minHeight) {
            if (resizeDirection == ResizeDirection.NORTH ||
                resizeDirection == ResizeDirection.NORTHEAST ||
                resizeDirection == ResizeDirection.NORTHWEST) {
                newY = stage.y + (startHeight - stage.minHeight)
            }
            newHeight = stage.minHeight
        }

        stage.x = newX
        stage.y = newY
        stage.width = newWidth
        stage.height = newHeight

        event.consume()
    }

    private fun handleMouseReleased(event: MouseEvent, stage: Stage, rootNode: Node) {
        if (resizing) {
            resizing = false
            resizeDirection = ResizeDirection.NONE
            rootNode.cursor = Cursor.DEFAULT
            logger.info("Completed window resize to: ${stage.width}x${stage.height}")
            event.consume()
        }
    }

    private fun getResizeDirection(event: MouseEvent, stage: Stage): ResizeDirection {
        val x = event.x
        val y = event.y
        val width = stage.width
        val height = stage.height

        val atLeft = x <= RESIZE_MARGIN
        val atRight = x >= width - RESIZE_MARGIN
        val atTop = y <= RESIZE_MARGIN
        val atBottom = y >= height - RESIZE_MARGIN

        return when {
            atTop && atLeft -> ResizeDirection.NORTHWEST
            atTop && atRight -> ResizeDirection.NORTHEAST
            atBottom && atLeft -> ResizeDirection.SOUTHWEST
            atBottom && atRight -> ResizeDirection.SOUTHEAST
            atTop -> ResizeDirection.NORTH
            atBottom -> ResizeDirection.SOUTH
            atLeft -> ResizeDirection.WEST
            atRight -> ResizeDirection.EAST
            else -> ResizeDirection.NONE
        }
    }

    /**
     * Check if a window is currently being resized
     */
    fun isCurrentlyResizing(): Boolean = resizing
}