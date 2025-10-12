package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.layout.HBox

class TopBarView : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TopBarView")
    }

    internal val toolBar: ToolBar

    init {
        setupTopBar()
        toolBar = createWindowControls()
        layoutComponents(toolBar)

        logger.info("Top bar initialized")
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