package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import net.fabricmc.loader.api.FabricLoader

class StatusBar : HBox() {

    companion object {
        private val LOGGER = LoggerProvider.getLogger("StatusBar")
        private val MOD_VERSION: String by lazy {
            FabricLoader.getInstance()
                .getModContainer("datapack-ide")
                .map { it.metadata.version.friendlyString }
                .orElse("unknown")
        }
    }

    val cursorPositionProperty = SimpleStringProperty("Ln 1, Col 1")
    val encodingProperty = SimpleStringProperty("UTF-8")
    val ideVersionProperty = SimpleStringProperty("DataPack IDE v$MOD_VERSION")

    private val cursorLabel = Label()
    private val encodingLabel = Label()
    private val ideVersionLabel = Label()

    init {
        styleClass.add("status-bar")
        CSSManager.applyToComponent(stylesheets, "StatusBar")
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
        cursorLabel.textProperty().bind(cursorPositionProperty)
        encodingLabel.textProperty().bind(encodingProperty)
        ideVersionLabel.textProperty().bind(ideVersionProperty)
    }

    private fun createSeparator(): Separator {
        return Separator(javafx.geometry.Orientation.VERTICAL).apply {
            styleClass.add("status-separator")
        }
    }

    fun updateCursorPosition(line: Int, column: Int) {
        cursorPositionProperty.set("Ln $line, Col $column")
        LOGGER.debug("Cursor position updated: Ln $line, Col $column")
    }

    fun updateEncoding(encoding: String) {
        encodingProperty.set(encoding)
        LOGGER.debug("Encoding updated: $encoding")
    }
}