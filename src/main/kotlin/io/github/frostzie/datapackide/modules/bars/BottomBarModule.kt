package io.github.frostzie.datapackide.modules.bars

import javafx.beans.property.SimpleStringProperty
import net.fabricmc.loader.api.FabricLoader

class BottomBarModule {
    //TODO: Move to Fabric utils to separate fabric dep from mod
    companion object {
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

    fun updateCursorPosition(line: Int, column: Int) {
        cursorPositionProperty.set("Ln $line, Col $column")
    }

    fun updateEncoding(encoding: String) {
        encodingProperty.set(encoding)
    }
}