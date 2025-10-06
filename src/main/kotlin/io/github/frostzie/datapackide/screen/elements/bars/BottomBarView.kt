package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.modules.bars.BottomBarModule
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class BottomBarView() : HBox() {

    companion object {
        private val LOGGER = LoggerProvider.getLogger("StatusBar")
    }

    private val module = BottomBarModule()
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
            setHgrow(this, Priority.ALWAYS)
            styleClass.add("status-spacer")
        }

        children.addAll(
            cursorLabel,
            createSeparator(),
            encodingLabel,
            createSeparator(),
            spacer,
            ideVersionLabel
        )
    }

    private fun bindProperties() {
        cursorLabel.textProperty().bind(module.cursorPositionProperty)
        encodingLabel.textProperty().bind(module.encodingProperty)
        ideVersionLabel.textProperty().bind(module.ideVersionProperty)
    }

    private fun createSeparator(): Separator {
        return Separator(javafx.geometry.Orientation.VERTICAL).apply {
            styleClass.add("status-separator")
        }
    }
}