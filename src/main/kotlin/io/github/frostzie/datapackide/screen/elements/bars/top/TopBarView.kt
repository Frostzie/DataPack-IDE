package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.IconButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowMaximize
import io.github.frostzie.datapackide.events.MainWindowRestore
import io.github.frostzie.datapackide.events.MenuControlsVisibilityChanged
import io.github.frostzie.datapackide.events.ReloadDatapack
import io.github.frostzie.datapackide.events.SettingsWindowOpen
import io.github.frostzie.datapackide.events.ToggleMenuControls
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class TopBarView : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TopBarView")
    }

    private val menuControls: MenuControls
    internal val windowControls: WindowControls
    private var hideMenuButton: IconButton
    private var runDataPackButton: IconButton
    private var settingsButton: IconButton

    init {
        setupTopBar()
        menuControls = createApplicationMenuBar()
        hideMenuButton = createHideMenuButton()
        runDataPackButton = createRunDataPackButton()
        settingsButton = createSettingsButton()
        windowControls = createWindowControls()

        layoutComponents(hideMenuButton, runDataPackButton, menuControls, settingsButton, windowControls)

        this.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                if (event.target == this || (event.target as? Region)?.styleClass?.contains("title-spacer") == true) {
                    if (windowControls.isMaximized) {
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
        styleClass.add("title-bar")
        prefHeight = UIConstants.TOP_BAR_HEIGHT
        minHeight = UIConstants.TOP_BAR_HEIGHT
        maxHeight = UIConstants.TOP_BAR_HEIGHT
    }

    private fun createHideMenuButton(): IconButton {
        return IconButton {
            styleClass.addAll("title-bar-icon-button", "hide-tools-icon")
            tooltip = Tooltip("Toggle Menu Bar")
            setOnAction {
                EventBus.post(ToggleMenuControls())
            }
        }
    }

    private fun createRunDataPackButton(): IconButton {
        return IconButton {
            styleClass.addAll("title-bar-icon-button", "run-datapack-icon")
            tooltip = Tooltip("Reload Datapack")
            setOnAction {
                EventBus.post(ReloadDatapack())
            }
        }
    }

    private fun createSettingsButton(): IconButton {
        return IconButton {
            styleClass.addAll("title-bar-icon-button", "settings-icon")
            tooltip = Tooltip("Settings")
            setOnAction {
                EventBus.post(SettingsWindowOpen())
            }
        }
    }

    private fun createApplicationMenuBar(): MenuControls {
        return MenuControls().apply {
            styleClass.add("title-menu-bar")
        }
    }

    private fun createWindowControls(): WindowControls {
        return WindowControls()
    }

    private fun layoutComponents(
        hideMenuButton: IconButton,
        runDataPackButton: IconButton,
        menuControls: MenuControls,
        settingsButton: IconButton,
        windowControls: WindowControls
    ) {
        val spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
        }

        children.addAll(hideMenuButton, menuControls, spacer, runDataPackButton, settingsButton, windowControls)
    }

    @SubscribeEvent
    fun onMenuBarVisibilityChanged(event: MenuControlsVisibilityChanged) {
        menuControls.isVisible = event.isVisible
        menuControls.isManaged = event.isVisible
    }
}