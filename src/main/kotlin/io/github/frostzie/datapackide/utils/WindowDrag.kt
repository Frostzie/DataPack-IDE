package io.github.frostzie.datapackide.utils

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximizedStateChanged
import io.github.frostzie.datapackide.events.MainWindowRestore
import io.github.frostzie.datapackide.screen.elements.bars.top.TopBarView
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import javafx.util.Duration

// Original code from: https://stackoverflow.com/questions/18792822/dragging-an-undecorated-stage-in-javafx
// Modified by Frostzie

object WindowDrag {
    private var isMaximized = false

    @Suppress("unused")
    @SubscribeEvent
    fun onMaximizedStateChanged(event: MainWindowMaximizedStateChanged) {
        isMaximized = event.isMaximized
    }

    fun makeDraggable(stage: Stage, byNode: Node) {
        val dragDelta = Delta()
        var isDragging = false
        val dragDelay = PauseTransition(Duration.millis(150.0)) // 1.5-second delay so normal clicks won't count as dragging
        var pressEvent: MouseEvent? = null

        EventBus.register(this) // This is just so it resizes the window when dragging from maximized

        dragDelay.onFinished = EventHandler { 
            pressEvent?.let { mouseEvent ->
                if (!isMaximized) return@let

                val originalMouseX = mouseEvent.screenX
                val originalStageWidth = stage.width
                val mouseXRatio = originalMouseX / originalStageWidth

                EventBus.post(MainWindowRestore())

                Platform.runLater {
                    val restoredWidth = stage.width
                    val newX = originalMouseX - (restoredWidth * mouseXRatio)
                    val newY = mouseEvent.screenY - mouseEvent.y

                    stage.x = newX
                    stage.y = if (newY < 0) 0.0 else newY

                    dragDelta.x = stage.x - mouseEvent.screenX
                    dragDelta.y = stage.y - mouseEvent.screenY
                    isDragging = true
                }
            }
        }

        val pressHandler = EventHandler<MouseEvent> { mouseEvent ->
            if (byNode is TopBarView) {
                if (!byNode.isOverDraggableArea(mouseEvent)) {
                    return@EventHandler
                }
            }

            if (isMaximized) {
                pressEvent = mouseEvent
                dragDelay.playFromStart()
            } else {
                dragDelta.x = stage.x - mouseEvent.screenX
                dragDelta.y = stage.y - mouseEvent.screenY
                isDragging = true
            }
        }

        val dragHandler = EventHandler<MouseEvent> { mouseEvent ->
            if (isDragging && !isMaximized) {
                stage.x = mouseEvent.screenX + dragDelta.x
                stage.y = mouseEvent.screenY + dragDelta.y
            }
        }

        val releaseHandler = EventHandler<MouseEvent> {
            dragDelay.stop()
            isDragging = false
        }

        byNode.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler)
        byNode.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler)
        byNode.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler)
    }

    private class Delta {
        var x: Double = 0.0
        var y: Double = 0.0
    }
}