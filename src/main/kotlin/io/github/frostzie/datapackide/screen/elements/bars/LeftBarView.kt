package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.events.ChooseDirectory
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.VBox
import org.kordamp.ikonli.feather.Feather
import org.kordamp.ikonli.javafx.FontIcon

class LeftBarView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("LeftBarView")
    }

    private val directoryChooseButton: Button
    private val searchButton: Button

    init {
        setupLeftBar()
        directoryChooseButton = directoryChooseButton()
        searchButton = searchButton()

        layoutComponents(directoryChooseButton, searchButton)
    }

    private fun setupLeftBar() {
        logger.debug("Left bar initialized")
    }

    private fun directoryChooseButton(): Button {
        return Button().apply {
            graphic = FontIcon(Feather.FOLDER)
            tooltip = Tooltip("Choose Directory")
            setOnAction {
                EventBus.post(ChooseDirectory())
            }
        }
    }

    private fun searchButton(): Button {
        return Button().apply {
            graphic = FontIcon(Feather.SEARCH)
            tooltip = Tooltip("Search")
            setOnAction {
                logger.info("Search button clicked") //TODO: Implement search
            }
        }
    }

    private fun layoutComponents(
        directoryChooseButton: Button,
        searchButton: Button,
    ) {
        children.addAll(directoryChooseButton, searchButton)
    }
}