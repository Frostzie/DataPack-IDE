package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.events.ChooseDirectory
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.Tooltip
import javafx.scene.layout.VBox

class LeftBarView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("LeftBarView")
    }

    private val directoryChooseButton: IconButton
    private val searchButton: IconButton

    init {
        setupLeftBar()
        directoryChooseButton = directoryChooseButton()
        searchButton = searchButton()

        layoutComponents(directoryChooseButton, searchButton)
    }

    private fun setupLeftBar() {
        styleClass.add("left-bar")
        logger.debug("Left bar initialized")
    }

    private fun directoryChooseButton(): IconButton {
        return IconButton {
            styleClass.addAll("left-bar-button", "directory-choose-icon")
            tooltip = Tooltip("Choose Directory")
            setOnAction {
                EventBus.post(ChooseDirectory())
            }
        }
    }

    private fun searchButton(): IconButton {
        return IconButton {
            styleClass.addAll("left-bar-button", "search-icon")
            tooltip = Tooltip("Search")
            setOnAction {
                logger.info("Search button clicked") //TODO: Implement search
            }
        }
    }

    private fun layoutComponents(
        directoryChooseButton: IconButton,
        searchButton: IconButton,
    ) {
        children.addAll(directoryChooseButton, searchButton)
    }
}