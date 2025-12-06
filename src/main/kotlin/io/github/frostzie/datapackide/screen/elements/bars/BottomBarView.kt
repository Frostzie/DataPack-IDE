package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.modules.bars.BottomBarModule
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.ToolBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class BottomBarView() : ToolBar() {

    companion object {
        private val LOGGER = LoggerProvider.getLogger("StatusBar")
    }

    private val cursorLabel = Label()
    private val encodingLabel = Label()
    private val ideVersionLabel = Label()

    init {
        styleClass.add("status-bar")
        createStatusElements()
        bindProperties()
        LOGGER.info("Status bar initialized")
    }

    private fun createStatusElements() {
        cursorLabel.styleClass.add("status-label")
        encodingLabel.styleClass.add("status-label")
        ideVersionLabel.styleClass.add("status-label")

        val spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            styleClass.add("status-spacer")
        }

        items.addAll(
            cursorLabel,
            createSeparator(),
            encodingLabel,
            createSeparator(),
            spacer,
            ideVersionLabel
        )
    }

    private fun bindProperties() {
        cursorLabel.textProperty().bind(BottomBarModule.cursorPositionProperty)
        encodingLabel.textProperty().bind(BottomBarModule.encodingProperty)
        ideVersionLabel.textProperty().bind(BottomBarModule.ideVersionProperty)
    }

    private fun createSeparator(): Separator {
        return Separator(javafx.geometry.Orientation.VERTICAL).apply {
            styleClass.add("status-separator")
        }
    }
}