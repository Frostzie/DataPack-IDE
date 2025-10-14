package io.github.frostzie.datapackide.utils

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.stage.Stage

object WindowResizer {

    fun install(stage: Stage, contentPane: Pane): StackPane {
        val wrapper = StackPane(contentPane)
        val borderThickness = 8.0 // A bit thicker for a testing period

        val top = createResizeRegion(Cursor.N_RESIZE, borderThickness, -1.0)
        val bottom = createResizeRegion(Cursor.S_RESIZE, borderThickness, -1.0)
        val left = createResizeRegion(Cursor.W_RESIZE, -1.0, borderThickness)
        val right = createResizeRegion(Cursor.E_RESIZE, -1.0, borderThickness)
        val nw = createResizeRegion(Cursor.NW_RESIZE, borderThickness, borderThickness)
        val ne = createResizeRegion(Cursor.NE_RESIZE, borderThickness, borderThickness)
        val sw = createResizeRegion(Cursor.SW_RESIZE, borderThickness, borderThickness)
        val se = createResizeRegion(Cursor.SE_RESIZE, borderThickness, borderThickness)

        val resizeData = ResizeData()

        setupResize(top, resizeData, stage, north = true)
        setupResize(bottom, resizeData, stage, south = true)
        setupResize(left, resizeData, stage, west = true)
        setupResize(right, resizeData, stage, east = true)
        setupResize(nw, resizeData, stage, north = true, west = true)
        setupResize(ne, resizeData, stage, north = true, east = true)
        setupResize(sw, resizeData, stage, south = true, west = true)
        setupResize(se, resizeData, stage, south = true, east = true)

        wrapper.children.addAll(top, bottom, left, right, nw, ne, sw, se)

        StackPane.setAlignment(top, Pos.TOP_CENTER)
        StackPane.setAlignment(bottom, Pos.BOTTOM_CENTER)
        StackPane.setAlignment(left, Pos.CENTER_LEFT)
        StackPane.setAlignment(right, Pos.CENTER_RIGHT)
        StackPane.setAlignment(nw, Pos.TOP_LEFT)
        StackPane.setAlignment(ne, Pos.TOP_RIGHT)
        StackPane.setAlignment(sw, Pos.BOTTOM_LEFT)
        StackPane.setAlignment(se, Pos.BOTTOM_RIGHT)

        return wrapper
    }

    private fun createResizeRegion(cursor: Cursor, height: Double, width: Double): Region {
        return Region().apply {
            this.cursor = cursor
            if (height != -1.0) {
                prefHeight = height
                maxHeight = height
            }
            if (width != -1.0) {
                prefWidth = width
                maxWidth = width
            }
            style = "-fx-background-color: rgba(255, 0, 0, 0.5);" //TODO: add this to dev settings
        }
    }

    private fun setupResize(
        region: Region,
        data: ResizeData,
        stage: Stage,
        north: Boolean = false,
        south: Boolean = false,
        west: Boolean = false,
        east: Boolean = false
    ) {
        region.onMousePressed = EventHandler { e ->
            data.x = e.screenX
            data.y = e.screenY
            data.width = stage.width
            data.height = stage.height
            data.stageX = stage.x
            data.stageY = stage.y
            e.consume()
        }

        region.onMouseDragged = EventHandler { e ->
            val deltaX = e.screenX - data.x
            val deltaY = e.screenY - data.y

            if (east) {
                val newWidth = data.width + deltaX
                if (newWidth > stage.minWidth) stage.width = newWidth
            }
            if (west) {
                val newWidth = data.width - deltaX
                if (newWidth > stage.minWidth) {
                    stage.width = newWidth
                    stage.x = data.stageX + deltaX
                }
            }
            if (south) {
                val newHeight = data.height + deltaY
                if (newHeight > stage.minHeight) stage.height = newHeight
            }
            if (north) {
                val newHeight = data.height - deltaY
                if (newHeight > stage.minHeight) {
                    stage.height = newHeight
                    stage.y = data.stageY + deltaY
                }
            }
            e.consume()
        }
    }

    private class ResizeData {
        var x: Double = 0.0
        var y: Double = 0.0
        var width: Double = 0.0
        var height: Double = 0.0
        var stageX: Double = 0.0
        var stageY: Double = 0.0
    }
}
