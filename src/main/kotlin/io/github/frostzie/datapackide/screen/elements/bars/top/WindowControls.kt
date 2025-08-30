package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.ColorUtils
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.geometry.Rectangle2D
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.Screen
import javafx.stage.Stage
import kotlin.system.exitProcess

class WindowControls(
    private val stage: Stage,
    private val isStandaloneMode: Boolean = false
) : HBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("WindowControls")
        private const val ICON_BASE_PATH = "/assets/datapack-ide/themes/icon/top-bar/window-controls/"

        private val minimizeIcon = Image(loadIconResource("minimize.png"))
        private val maximizeIcon = Image(loadIconResource("maximize.png"))
        private val restoreIcon = Image(loadIconResource("restore-down.png"))
        private val closeIcon = Image(loadIconResource("close.png"))

        private fun loadIconResource(name: String): String {
            return WindowControls::class.java.getResource(ICON_BASE_PATH + name)?.toExternalForm()
                ?: throw IllegalArgumentException("Window control icon not found: $name")
        }
    }

    private var previousBounds: Rectangle2D? = null

    init {
        setupWindowControls()
        createButtons()
        logger.info("Window controls initialized")
    }

    private fun setupWindowControls() {
        styleClass.add("window-controls")
        CSSManager.applyToComponent(stylesheets, "WindowControls")
    }

    private fun createButtons() {
        val minimizeButton = createMinimizeButton()
        val maximizeButton = createMaximizeButton()
        val closeButton = createCloseButton()

        children.addAll(minimizeButton, maximizeButton, closeButton)
    }

    /**
     * Applies color effects to an icon based on CSS custom properties.
     */
    private fun applyIconBehavior(button: Button, iconView: ImageView, buttonStyleClass: String) {
        val defaultColor = extractCSSCustomProperty(buttonStyleClass, "-icon-color") ?: "#f0eded"
        val hoverColor = extractCSSCustomProperty("$buttonStyleClass:hover", "-icon-hover-color") ?: defaultColor

        val defaultEffect = ColorUtils.createColorAdjustForWhiteIcon(defaultColor)
        val hoverEffect = ColorUtils.createColorAdjustForWhiteIcon(hoverColor)

        iconView.effect = defaultEffect

        button.hoverProperty().addListener { _, _, isHovering ->
            iconView.effect = if (isHovering) hoverEffect else defaultEffect
        }
    }

    private fun extractCSSCustomProperty(cssClass: String, propertyName: String): String? {
        return CSSManager.parseCSSCustomProperty(cssClass, propertyName)
    }

    private fun createMinimizeButton(): Button {
        return Button().apply {
            val iconView = ImageView(minimizeIcon)
            graphic = iconView
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            styleClass.addAll("window-control-button", "minimize-button")
            applyIconBehavior(this, iconView, "minimize-button")
            setOnAction {
                stage.isIconified = true
                logger.debug("Window minimized")
            }
        }
    }

    private fun createMaximizeButton(): Button {
        val maximizeIconView = ImageView(maximizeIcon)
        val restoreIconView = ImageView(restoreIcon)

        return Button().apply {
            graphic = if (isStageMaximized()) restoreIconView else maximizeIconView
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            styleClass.addAll("window-control-button", "maximize-button")
            applyIconBehavior(this, maximizeIconView, "maximize-button")
            applyIconBehavior(this, restoreIconView, "maximize-button")
            setOnAction {
                toggleMaximize()
                graphic = if (isStageMaximized()) restoreIconView else maximizeIconView
            }
        }
    }

    private fun isStageMaximized(): Boolean {
        val screenBounds = Screen.getPrimary().visualBounds
        return stage.x == screenBounds.minX &&
                stage.y == screenBounds.minY &&
                stage.width == screenBounds.width &&
                stage.height == screenBounds.height
    }

    private fun toggleMaximize() {
        val screenBounds = Screen.getPrimary().visualBounds
        if (isStageMaximized()) {
            previousBounds?.let {
                stage.x = it.minX; stage.y = it.minY; stage.width = it.width; stage.height = it.height
            }
            logger.debug("Window restored from maximized state")
        } else {
            previousBounds = Rectangle2D(stage.x, stage.y, stage.width, stage.height)
            stage.x = screenBounds.minX
            stage.y = screenBounds.minY
            stage.width = screenBounds.width
            stage.height = screenBounds.height
            logger.debug("Window maximized to visual bounds")
        }
    }

    private fun createCloseButton(): Button {
        return Button().apply {
            val iconView = ImageView(closeIcon)
            graphic = iconView
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            styleClass.addAll("window-control-button", "close-button")
            applyIconBehavior(this, iconView, "close-button")
            setOnAction {
                if (isStandaloneMode) {
                    Platform.exit()
                    exitProcess(0)
                } else {
                    stage.hide()
                    logger.info("Window hidden via window controls close button")
                }
            }
        }
    }
}