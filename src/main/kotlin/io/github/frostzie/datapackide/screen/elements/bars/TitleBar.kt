package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Button
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
    private var xOffset = 0.0
    private var yOffset = 0.0

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
        val windowControls = createWindowControls()

        layoutComponents(menuBar, windowControls)
        setupDragHandling()
        logger.info("Title bar initialized")
    }

    private fun setupTitleBar() {
        styleClass.add("title-bar")
        stylesheets.add(javaClass.getResource("/assets/datapack-ide/themes/TitleBar.css")?.toExternalForm())
        alignment = Pos.CENTER_LEFT
        spacing = 10.0
        prefHeight = 32.0
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

    //TODO: Change to svg or better icons at least
    private fun createWindowControls(): HBox {
        val windowControls = HBox().apply {
            alignment = Pos.CENTER_RIGHT
            spacing = 0.0
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

    private fun layoutComponents(menuBar: MenuBar, windowControls: HBox) {
        val spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
        }

        children.addAll(menuBar, spacer, windowControls)
    }

    private fun setupDragHandling() {
        setOnMousePressed { event: MouseEvent ->
            if (event.isPrimaryButtonDown && !isClickOnControl(event)) {
                xOffset = event.sceneX
                yOffset = event.sceneY
                logger.debug("Drag started at: ${event.sceneX}, ${event.sceneY}")
            }
        }

        setOnMouseDragged { event: MouseEvent ->
            if (event.isPrimaryButtonDown && !isClickOnControl(event)) {
                if (stage.isMaximized) {
                    stage.isMaximized = false
                    xOffset = stage.width / 2
                    logger.debug("Window restored from maximized during drag")
                }
                stage.x = event.screenX - xOffset
                stage.y = event.screenY - yOffset
            }
        }

        setOnMouseClicked { event ->
            if (event.clickCount == 2 && event.isPrimaryButtonDown && !isClickOnControl(event)) {
                stage.isMaximized = !stage.isMaximized
                logger.debug("Window maximize toggled via double-click: ${stage.isMaximized}")
            }
        }
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