package io.github.frostzie.datapackide.utils

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.events.MainWindowRestore
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.Screen
import javafx.stage.Stage

/**
 * Utility class to handle drag event forwarding for custom window controls.
 * This allows JavaFX controls (like ToolBar, MenuBar) to forward mouse events
 * to the scene-level ResizeHandler while maintaining their own interactivity.
 */
class DragForwarding(
    private val targetNode: Node,
    private val stage: Stage?,
    private val draggableAreaChecker: (MouseEvent) -> Boolean
) {

    companion object {
        private val logger = LoggerProvider.getLogger("DragForwarding")
    }

    private var isMaximized = false
    private var dragStartX = 0.0
    private var dragStartY = 0.0
    private var isDraggingFromMaximized = false
    private var previousBounds: Rectangle2D? = null

    // New state for handling drag-after-restore
    private var isRestoredAndDragging = false
    private var dragStartStageX = 0.0
    private var dragStartStageY = 0.0

    /**
     * Install all necessary event handlers on the target node
     */
    fun install() {
        setupMouseMovedForwarding()
        setupMousePressedForwarding()
        setupMouseDraggedForwarding()
        setupMouseReleasedForwarding()
        setupDoubleClickHandler()

        logger.debug("DragForwardingHandler installed on ${targetNode.javaClass.simpleName}")
    }

    fun setMaximizedState(maximized: Boolean) {
        isMaximized = maximized
    }

    private fun setupMouseMovedForwarding() {
        targetNode.setOnMouseMoved { event ->
            if (draggableAreaChecker(event)) {
                forwardEventToScene(event)
            }
        }
    }

    private fun setupMousePressedForwarding() {
        targetNode.setOnMousePressed { event ->
            if (event.button == MouseButton.PRIMARY) {
                if (draggableAreaChecker(event)) {
                    dragStartX = event.screenX
                    dragStartY = event.screenY

                    if (isMaximized) {
                        isDraggingFromMaximized = true
                    } else {
                        isDraggingFromMaximized = false
                        isRestoredAndDragging = false // Reset state
                        forwardEventToScene(event)
                    }
                } else {
                    isDraggingFromMaximized = false
                }
            }
        }
    }

    private fun setupMouseDraggedForwarding() {
        targetNode.setOnMouseDragged { event ->
            if (event.button == MouseButton.PRIMARY) {
                if (isRestoredAndDragging) {
                    // Special drag-after-restore mode
                    val dX = event.screenX - dragStartX
                    val dY = event.screenY - dragStartY
                    stage?.let {
                        it.x = dragStartStageX + dX
                        it.y = dragStartStageY + dY
                    }
                } else if (draggableAreaChecker(event)) {
                    if (isDraggingFromMaximized) {
                        // This is the first drag event from maximized. Restore and switch to our special drag mode.
                        restoreWindowUnderCursor(event)
                        isDraggingFromMaximized = false
                        isRestoredAndDragging = true

                        // Record the initial stage position after restore.
                        stage?.let {
                            dragStartStageX = it.x
                            dragStartStageY = it.y
                        }
                    } else {
                        // Normal drag, forward to ResizeHandler.
                        forwardEventToScene(event)
                    }
                }
            }
        }
    }

    private fun setupMouseReleasedForwarding() {
        targetNode.setOnMouseReleased { event ->
            if (event.button == MouseButton.PRIMARY) {
                if (draggableAreaChecker(event) || isRestoredAndDragging) {
                    forwardEventToScene(event)
                }

                isDraggingFromMaximized = false
                isRestoredAndDragging = false
                dragStartX = 0.0
                dragStartY = 0.0
            }
        }
    }

    private fun setupDoubleClickHandler() {
        targetNode.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                if (draggableAreaChecker(event)) {
                    if (isMaximized) {
                        EventBus.post(MainWindowRestore())
                    } else {
                        EventBus.post(MainWindowMaximize())
                    }
                    event.consume()
                }
            }
        }
    }

    /**
     * Restore window from maximized state and position it under the cursor
     */
    private fun restoreWindowUnderCursor(event: MouseEvent) {
        stage?.let { stg ->
            val screen = Screen.getScreensForRectangle(stg.x, stg.y, stg.width, stg.height).firstOrNull()
                ?: Screen.getPrimary()
            val visualBounds = screen.visualBounds

            val currentWidth = stg.width

            val restoredWidth = previousBounds?.width ?: 1200.0
            val restoredHeight = previousBounds?.height ?: 800.0

            val mouseXRatio = event.screenX / currentWidth

            val newX = event.screenX - (restoredWidth * mouseXRatio)
            val newY = event.screenY - 20.0 // Keep some offset from the top

            val clampedX = newX.coerceIn(visualBounds.minX, visualBounds.maxX - restoredWidth)
            val clampedY = newY.coerceIn(visualBounds.minY, visualBounds.maxY - restoredHeight)

            stg.x = clampedX
            stg.y = clampedY
            stg.width = restoredWidth
            stg.height = restoredHeight

            isMaximized = false
            EventBus.post(MainWindowMaximizedStateChanged(false))

            logger.debug("Window restored under cursor at (${clampedX}, ${clampedY})")
        }
    }

    /**
     * Store the previous window bounds (call this before maximizing)
     */
    fun storePreviousBounds(bounds: Rectangle2D) {
        previousBounds = bounds
    }

    /**
     * Forward a mouse event to the scene root so ResizeHandler can process it
     */
    private fun forwardEventToScene(event: MouseEvent) {
        targetNode.scene?.let { currentScene ->
            val newEvent = MouseEvent(
                event.source,
                currentScene.root,
                event.eventType,
                event.x,
                event.y,
                event.screenX,
                event.screenY,
                event.button,
                event.clickCount,
                event.isShiftDown,
                event.isControlDown,
                event.isAltDown,
                event.isMetaDown,
                event.isPrimaryButtonDown,
                event.isMiddleButtonDown,
                event.isSecondaryButtonDown,
                event.isSynthesized,
                event.isPopupTrigger,
                event.isStillSincePress,
                event.pickResult
            )

            currentScene.root.fireEvent(newEvent)
        }
    }
}