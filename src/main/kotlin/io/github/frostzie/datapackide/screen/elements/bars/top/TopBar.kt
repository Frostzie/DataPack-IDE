package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand.executeCommandButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import org.kordamp.ikonli.javafx.FontIcon
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.Stage

class TopBar(private val stage: Stage, private val isStandaloneMode: Boolean = false) : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TopBar")
    }

    private val toolControls: ToolControls
    private val windowControls: WindowControls
    private var menuBarVisible = true
    private var hideMenuButton: Button
    private var runDataPackButton: Button
    private var settingsButton: Button

    var onNewFile: (() -> Unit)? = null
    var onOpenFile: (() -> Unit)? = null
    var onSaveFile: (() -> Unit)? = null
    var onSaveAsFile: (() -> Unit)? = null
    var onCloseFile: (() -> Unit)? = null
    var onExit: (() -> Unit)? = null
    var onUndo: (() -> Unit)? = null
    var onRedo: (() -> Unit)? = null
    var onCut: (() -> Unit)? = null
    var onCopy: (() -> Unit)? = null
    var onPaste: (() -> Unit)? = null
    var onFind: (() -> Unit)? = null
    var onReplace: (() -> Unit)? = null
    var onRunDatapack: (() -> Unit)? = null
    var onValidateDatapack: (() -> Unit)? = null
    var onPackageDatapack: (() -> Unit)? = null
    var onPreferences: (() -> Unit)? = null
    var onAbout: (() -> Unit)? = null

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
        logger.info("Top bar initialized")
    }

    private fun setupTopBar() {
        styleClass.add("title-bar")
        prefHeight = UIConstants.TOP_BAR_HEIGHT
        minHeight = UIConstants.TOP_BAR_HEIGHT
        maxHeight = UIConstants.TOP_BAR_HEIGHT
    }

    private fun createHideMenuButton(): Button {
        return Button().apply {
            styleClass.addAll("hide-tools-button", "title-bar-icon-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            tooltip = Tooltip("Toggle Menu Bar")

            val icon = FontIcon().apply {
                styleClass.add("hide-tools-icon")
            }

            graphic = icon
            setOnAction { toggleMenuBar() }
        }
    }

    private fun createRunDataPackButton(): Button {
        return Button().apply {
            styleClass.addAll("run-datapack-button", "title-bar-icon-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            tooltip = Tooltip("Reload Datapack")

            val icon = FontIcon().apply {
                styleClass.add("run-datapack-icon")
            }

            graphic = icon
            setOnAction { executeCommandButton() }
        }
    }

    private fun createSettingsButton(): Button {
        return Button().apply {
            styleClass.addAll("settings-button", "title-bar-icon-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            tooltip = Tooltip("Settings")

            val icon = FontIcon().apply {
                styleClass.add("settings-icon")
            }

            graphic = icon
            setOnAction { onPreferences?.invoke() }
        }
    }

    private fun createApplicationMenuBar(): ToolControls {
        return ToolControls().apply {
            styleClass.add("title-menu-bar")

            // Set up all the menu callbacks
            onNewFile = { this@TopBar.onNewFile?.invoke() }
            onOpenFile = { this@TopBar.onOpenFile?.invoke() }
            onSaveFile = { this@TopBar.onSaveFile?.invoke() }
            onSaveAsFile = { this@TopBar.onSaveAsFile?.invoke() }
            onCloseFile = { this@TopBar.onCloseFile?.invoke() }
            onExit = { this@TopBar.onExit?.invoke() }
            onUndo = { this@TopBar.onUndo?.invoke() }
            onRedo = { this@TopBar.onRedo?.invoke() }
            onCut = { this@TopBar.onCut?.invoke() }
            onCopy = { this@TopBar.onCopy?.invoke() }
            onPaste = { this@TopBar.onPaste?.invoke() }
            onFind = { this@TopBar.onFind?.invoke() }
            onReplace = { this@TopBar.onReplace?.invoke() }
            onRunDatapack = { this@TopBar.onRunDatapack?.invoke() }
            onValidateDatapack = { this@TopBar.onValidateDatapack?.invoke() }
            onPackageDatapack = { this@TopBar.onPackageDatapack?.invoke() }
            onPreferences = { this@TopBar.onPreferences?.invoke() }
            onAbout = { this@TopBar.onAbout?.invoke() }
        }
    }

    private fun createWindowControls(): WindowControls {
        return WindowControls(stage, isStandaloneMode)
    }

    private fun layoutComponents(
        hideMenuButton: Button,
        runDataPackButton: Button,
        toolControls: ToolControls,
        settingsButton: Button,
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
        logger.info("Menu bar visibility toggled: $menuBarVisible")
    }

    fun getApplicationMenuBar(): ToolControls = toolControls
}