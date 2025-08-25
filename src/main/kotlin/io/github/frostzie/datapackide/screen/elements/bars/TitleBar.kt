package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.commands.ReloadDataPacksCommand.executeCommandButton
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Tooltip
import javafx.scene.effect.ColorAdjust
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.Stage
import kotlin.system.exitProcess

class TitleBar(private val stage: Stage, private val isStandaloneMode: Boolean = false) : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TitleBar")
    }

    private val menuBar: MenuBar
    private var menuBarVisible = true
    private var hideToolsButton: Button
    private var runDataPackButton: Button
    private var settingsButton: Button

    // Callbacks for menu actions - will be set by MainApplication
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
        setupTitleBar()
        menuBar = createMenuBar()
        hideToolsButton = createHideToolsButton()
        runDataPackButton = createRunDataPackButton()
        settingsButton = createSettingsButton()
        val windowControls = createWindowControls()

        layoutComponents(hideToolsButton, runDataPackButton, menuBar, settingsButton, windowControls)
        logger.info("Title bar initialized")
    }

    private fun setupTitleBar() {
        styleClass.add("title-bar")
        CSSManager.applyToComponent(stylesheets, "TitleBar")
    }

    private fun createHideToolsButton(): Button {
        return Button().apply {
            styleClass.addAll("hide-tools-button", "window-control-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            tooltip = Tooltip("Toggle Menu Bar")
            //TODO: remove hardcoded path
            val iconPath = "/assets/datapack-ide/themes/icon/HideTools.png"
            val iconStream = javaClass.getResourceAsStream(iconPath)
                ?: throw IllegalArgumentException("Icon not found: $iconPath")

            val imageView = ImageView(Image(iconStream)).apply {
                isPreserveRatio = true
                styleClass.add("hide-tools-icon")

                effect = ColorAdjust(0.0, 0.0, 1.0, 0.0)
            }

            graphic = imageView
            setOnAction { toggleMenuBar() }
        }
    }

    private fun createRunDataPackButton(): Button {
        return Button().apply {
            styleClass.addAll("run-datapack-button", "window-control-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            tooltip = Tooltip("Reload Datapack")
            //TODO: remove hardcoded path
            val iconPath = "/assets/datapack-ide/themes/icon/play.png"
            val iconStream = javaClass.getResourceAsStream(iconPath)
                ?: throw IllegalArgumentException("Icon not found: $iconPath")

            val imageView = ImageView(Image(iconStream)).apply {
                isPreserveRatio = true
                styleClass.add("run-datapack-icon")

                effect = ColorAdjust(0.0, 0.0, 1.0, 0.0)
            }

            graphic = imageView
            setOnAction { executeCommandButton() }
        }
    }

    private fun createSettingsButton(): Button {
        return Button().apply {
            styleClass.addAll("settings-button", "window-control-button")
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            tooltip = Tooltip("Settings")
            //TODO: remove hardcoded path
            val iconPath = "/assets/datapack-ide/themes/icon/settings.png"
            val iconStream = javaClass.getResourceAsStream(iconPath)
                ?: throw IllegalArgumentException("Icon not found: $iconPath")

            val imageView = ImageView(Image(iconStream)).apply {
                isPreserveRatio = true
                styleClass.add("settings-icon")
                effect = ColorAdjust(0.0, 0.0, 1.0, 0.0)
            }

            graphic = imageView
            setOnAction { onPreferences?.invoke() }
        }
    }

    private fun createMenuBar(): MenuBar {
        return MenuBar().apply {
            styleClass.add("title-menu-bar")

            // Set up all the menu callbacks
            onNewFile = { this@TitleBar.onNewFile?.invoke() }
            onOpenFile = { this@TitleBar.onOpenFile?.invoke() }
            onSaveFile = { this@TitleBar.onSaveFile?.invoke() }
            onSaveAsFile = { this@TitleBar.onSaveAsFile?.invoke() }
            onCloseFile = { this@TitleBar.onCloseFile?.invoke() }
            onExit = { this@TitleBar.onExit?.invoke() }
            onUndo = { this@TitleBar.onUndo?.invoke() }
            onRedo = { this@TitleBar.onRedo?.invoke() }
            onCut = { this@TitleBar.onCut?.invoke() }
            onCopy = { this@TitleBar.onCopy?.invoke() }
            onPaste = { this@TitleBar.onPaste?.invoke() }
            onFind = { this@TitleBar.onFind?.invoke() }
            onReplace = { this@TitleBar.onReplace?.invoke() }
            onRunDatapack = { this@TitleBar.onRunDatapack?.invoke() }
            onValidateDatapack = { this@TitleBar.onValidateDatapack?.invoke() }
            onPackageDatapack = { this@TitleBar.onPackageDatapack?.invoke() }
            onPreferences = { this@TitleBar.onPreferences?.invoke() }
            onAbout = { this@TitleBar.onAbout?.invoke() }
        }
    }

    private fun createWindowControls(): HBox {
        val windowControls = HBox().apply {
            styleClass.add("window-controls")
        }

        val minimizeButton = Button().apply {
            text = "\uD83D\uDDD5"
            styleClass.addAll("window-control-button", "minimize-button")
            setOnAction {
                stage.isIconified = true
                logger.debug("Window minimized")
            }
        }

        val maximizeButton = Button().apply {
            text = "\uD83D\uDDD6"
            styleClass.addAll("window-control-button", "maximize-button")
            setOnAction {
                stage.isMaximized = !stage.isMaximized
                text = if (stage.isMaximized) "\uD83D\uDDD7" else "\uD83D\uDDD6"
                logger.debug("Window maximize toggled: ${stage.isMaximized}")
            }
        }

        val closeButton = Button().apply {
            text = "\uD83D\uDDD9"
            styleClass.addAll("window-control-button", "close-button")
            setOnAction {
                if (isStandaloneMode) {
                    Platform.exit()
                    exitProcess(0)
                } else {
                    stage.hide()
                    logger.info("Window hidden via title bar close button")
                }
            }
        }

        windowControls.children.addAll(minimizeButton, maximizeButton, closeButton)
        return windowControls
    }

    private fun layoutComponents(hideToolsButton: Button, runDataPackButton: Button, menuBar: MenuBar, settingsButton: Button, windowControls: HBox) {
        val spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
        }

        children.addAll(hideToolsButton, menuBar, spacer, runDataPackButton, settingsButton, windowControls)
    }

    private fun toggleMenuBar() {
        menuBarVisible = !menuBarVisible
        menuBar.isVisible = menuBarVisible
        menuBar.isManaged = menuBarVisible
        logger.info("Menu bar visibility toggled: $menuBarVisible")
    }

    private fun isClickOnControl(event: MouseEvent): Boolean {
        val target = event.target
        return when {
            target.toString().contains("MenuButton") -> true
            target.toString().contains("Button") -> true
            target.toString().contains("MenuItem") -> true
            else -> false
        }
    }

    fun getMenuBar(): MenuBar = menuBar
}