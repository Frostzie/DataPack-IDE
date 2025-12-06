package io.github.frostzie.datapackide.modules.bars

import io.github.frostzie.datapackide.loader.fabric.ModVersion
import javafx.beans.property.SimpleStringProperty

object BottomBarModule {

    val cursorPositionProperty = SimpleStringProperty("")
    val encodingProperty = SimpleStringProperty("UTF-8")
    val ideVersionProperty = SimpleStringProperty("DataPack IDE v${ModVersion.current}")

    fun updateCursorPosition(line: Int, column: Int) {
        cursorPositionProperty.set("Ln $line, Col $column")
    }

    fun updateEncoding(encoding: String) {
        encodingProperty.set(encoding)
    }
}