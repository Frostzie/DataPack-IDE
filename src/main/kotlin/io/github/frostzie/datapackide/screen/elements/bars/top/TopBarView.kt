package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowRestore
import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Region

class TopBarView : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TopBarView")
    }

    internal val toolBar: ToolBar

    init {
        setupTopBar()
        toolBar = createWindowControls()

        layoutComponents(toolBar)

        this.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                if (event.target == this || (event.target as? Region)?.styleClass?.contains("title-spacer") == true) {
                    if (toolBar.isMaximized) {
                        EventBus.post(MainWindowRestore())
                    } else {
                        EventBus.post(MainWindowMaximize())
                    }
                }
            }
        }
        logger.info("Top bar initialized with event system")
    }

    private fun setupTopBar() {
        prefHeight = UIConstants.TOP_BAR_HEIGHT
        minHeight = UIConstants.TOP_BAR_HEIGHT
        maxHeight = UIConstants.TOP_BAR_HEIGHT
    }

    private fun createWindowControls(): ToolBar {
        return ToolBar()
    }

    private fun layoutComponents(
        toolBar: ToolBar
    ) {
        children.addAll(toolBar)
    }
}