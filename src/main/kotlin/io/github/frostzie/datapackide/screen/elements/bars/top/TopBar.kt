package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MenuActionEvent
import io.github.frostzie.datapackide.events.MenuCategory
import io.github.frostzie.datapackide.events.MenuAction
import io.github.frostzie.datapackide.events.MenuVisibilityEvent
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.Stage

class TopBar(private val stage: Stage) : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TopBar")
    }

    private val toolControls: ToolControls
    private val windowControls: WindowControls
    private var menuBarVisible = true
    private var hideMenuButton: IconButton
    private var runDataPackButton: IconButton
    private var settingsButton: IconButton

    init {
        setupTopBar()
        toolControls = createApplicationMenuBar()
        hideMenuButton = createHideMenuButton()
        runDataPackButton = createRunDataPackButton()
        settingsButton = createSettingsButton()
        windowControls = createWindowControls()

        layoutComponents(hideMenuButton, runDataPackButton, toolControls, settingsButton, windowControls)

        this.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                if (event.target == this || (event.target as? Region)?.styleClass?.contains("title-spacer") == true) {
                    windowControls.toggleMaximize()
                }
            }
        }
        logger.info("Top bar initialized with event system")
    }

    private fun setupTopBar() {
        styleClass.add("title-bar")
        prefHeight = UIConstants.TOP_BAR_HEIGHT
        minHeight = UIConstants.TOP_BAR_HEIGHT
        maxHeight = UIConstants.TOP_BAR_HEIGHT
    }

    private fun createHideMenuButton(): IconButton {
        return IconButton {
            styleClass.addAll("hide-tools-button", "title-bar-icon-button", "hide-tools-icon")
            tooltip = Tooltip("Toggle Menu Bar")
            setOnAction { toggleMenuBar() }
        }
    }

    private fun createRunDataPackButton(): IconButton {
        return IconButton {
            styleClass.addAll("run-datapack-button", "title-bar-icon-button", "run-datapack-icon")
            tooltip = Tooltip("Reload Datapack")
            setOnAction {
                EventBus.post(MenuActionEvent(MenuCategory.DATAPACK, MenuAction.RELOAD_DATAPACKS))
            }
        }
    }

    private fun createSettingsButton(): IconButton {
        return IconButton {
            styleClass.addAll("settings-button", "title-bar-icon-button", "settings-icon")
            tooltip = Tooltip("Settings")
            setOnAction {
                EventBus.post(MenuActionEvent(MenuCategory.HELP, MenuAction.PREFERENCES))
            }
        }
    }

    private fun createApplicationMenuBar(): ToolControls {
        return ToolControls().apply {
            styleClass.add("title-menu-bar")
        }
    }

    private fun createWindowControls(): WindowControls {
        return WindowControls(stage)
    }

    private fun layoutComponents(
        hideMenuButton: IconButton,
        runDataPackButton: IconButton,
        toolControls: ToolControls,
        settingsButton: IconButton,
        windowControls: WindowControls
    ) {
        val spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
        }

        children.addAll(hideMenuButton, toolControls, spacer, runDataPackButton, settingsButton, windowControls)
    }

    private fun toggleMenuBar() {
        menuBarVisible = !menuBarVisible
        toolControls.isVisible = menuBarVisible
        toolControls.isManaged = menuBarVisible
        EventBus.post(MenuVisibilityEvent(visible = menuBarVisible))
        logger.info("Menu bar visibility toggled: $menuBarVisible")
    }
}