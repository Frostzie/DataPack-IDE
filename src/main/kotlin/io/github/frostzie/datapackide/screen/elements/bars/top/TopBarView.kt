package io.github.frostzie.datapackide.screen.elements.bars.top

import atlantafx.base.theme.Styles
import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.loader.fabric.WorldDetection
import io.github.frostzie.datapackide.modules.bars.top.TopBarViewModel
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.feather.Feather
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import io.github.frostzie.datapackide.utils.UIConstants
import org.kordamp.ikonli.material2.Material2MZ
import org.kordamp.ikonli.material2.Material2OutlinedMZ
import io.github.frostzie.datapackide.utils.IconUtils
import io.github.frostzie.datapackide.styling.common.IconSource

class TopBarView(private val viewModel: TopBarViewModel) : HBox() {

    private val maximizeButton: Button
    private val spacer: Region
    private val logger = LoggerProvider.getLogger("TopBarView")

    init {
        alignment = Pos.CENTER_LEFT
        setHgrow(this, Priority.ALWAYS)
        prefHeight = UIConstants.TOP_BAR_HEIGHT
        minHeight = UIConstants.TOP_BAR_HEIGHT
        maxHeight = UIConstants.TOP_BAR_HEIGHT
        styleClass.add("top-bar-view")
        styleClass.add("menu-bar")

        val appIcon = IconUtils.createIcon(IconSource.SvgIcon("/assets/datapack-ide/svg/icon.svg", 25))
        setMargin(appIcon, Insets(0.0, 8.0, 0.0, 8.0))

        setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                viewModel.toggleMaximize()
            }
        }

        spacer = Region().apply {
            setHgrow(this, Priority.ALWAYS)
            styleClass.add("title-spacer")
        }

        val menuBar = createMenuBar()
        menuBar.isVisible = false
        menuBar.isManaged = false

        val toggleMenuBar = createTopBarButton(
            Material2OutlinedMZ.REORDER,
            "Toggle Menu Bar",
            Styles.BUTTON_OUTLINED
        ) {
            val newState = !menuBar.isVisible
            menuBar.isVisible = newState
            menuBar.isManaged = newState
        }

        val runDataPackButton = createTopBarButton(
            Feather.PLAY,
            "Reload Datapack"
        ) {
            EventBus.post(SaveAllFiles())
            EventBus.post(ReloadDatapack())
        }
        runDataPackButton.isDisable = !WorldDetection.isWorldOpen()

        val settingsButton = createTopBarButton(
            Material2MZ.SETTINGS,
            "Settings"
        ) {
            EventBus.post(SettingsWindowOpen())
        }

        val minimizeButton = createTopBarButton(
            Material2OutlinedMZ.MINIMIZE,
            "Minimize"
        ) {
            EventBus.post(MainWindowMinimize())
        }

        maximizeButton = createTopBarButton(
            Material2AL.CROP_SQUARE,
            "Maximize/Restore"
        ) {
            EventBus.post(MainWindowToggleMaximize())
        }

        val closeButton = createTopBarButton(
            Material2AL.CLOSE,
            "Close",
            Styles.DANGER
        ) {
            EventBus.post(MainWindowClose())
        }

        updateMaximizeButtonIcon(false)

        children.addAll(
            appIcon,
            toggleMenuBar,
            menuBar,
            spacer,
            runDataPackButton,
            settingsButton,
            minimizeButton,
            maximizeButton,
            closeButton
        )
    }

    private fun createMenuBar(): MenuBar {
        val menuBar = MenuBar()
        menuBar.menus.addAll(

            Menu("File", null,
                MenuItem("Open").apply { setOnAction { EventBus.post(ChooseDirectory()) } },
                MenuItem("New Project").apply { setOnAction { logger.warn("New Project button not implemented yet! ") } },
                MenuItem("Close Project").apply { setOnAction { logger.warn("Close Project button not implemented yet!") } },
                MenuItem("Save All").apply { setOnAction { EventBus.post(SaveAllFiles()) } },
                MenuItem("Exit").apply { setOnAction { EventBus.post(MainWindowClose()) } }
            ),

            Menu("Edit", null,
                MenuItem("Undo").apply { setOnAction { EventBus.post(EditorUndo()) } },
                MenuItem("Redo").apply { setOnAction { EventBus.post(EditorRedo()) } },
                MenuItem("Cut").apply { setOnAction { EventBus.post(EditorCut()) } },
                MenuItem("Copy").apply { setOnAction { EventBus.post(EditorCopy()) } },
                MenuItem("Find").apply { setOnAction { EventBus.post(EditorFind()) } },
                MenuItem("Select All").apply { setOnAction { EventBus.post(EditorSelectAll()) } }
            ),

            Menu("Build", null,
                MenuItem("Reload Datapack").apply {
                    setOnAction { EventBus.post(ReloadDatapack()) }
                    isDisable = !WorldDetection.isWorldOpen()
                },
                MenuItem("Zip Datapack").apply { setOnAction { logger.warn("Zip Datapack button not implemented yet!") } },
                MenuItem("Open Folder").apply { setOnAction { EventBus.post(OpenDatapackFolder()) } }
            ),

            Menu("Help", null,
                MenuItem("Discord").apply { setOnAction { EventBus.post(DiscordLink()) } },
                MenuItem("Github").apply { setOnAction { EventBus.post(GitHubLink()) } },
                MenuItem("Report a Bug").apply { setOnAction { EventBus.post(ReportBugLink()) } }
            )
        )
        return menuBar
    }

    /**
     * Check if a mouse event is over a draggable area (spacer or empty toolbar space)
     * This method is used by DragForwarding utility.
     */
    fun isOverDraggableArea(event: MouseEvent): Boolean {
        val target = event.target

        if (target == spacer || (target as? Region)?.styleClass?.contains("title-spacer") == true) {
            return true
        }

        if (target == this) {
            val localPoint = sceneToLocal(event.sceneX, event.sceneY)
            for (item in children) {
                if (item is Region && item != spacer) {
                    val bounds = item.boundsInParent
                    if (bounds.contains(localPoint)) {
                        return false
                    }
                }
            }
            return true
        }

        return false
    }

    private fun createTopBarButton(icon: Ikon, tooltipText: String, vararg styleClasses: String, action: () -> Unit): Button {
        return Button().apply {
            graphic = FontIcon(icon)
            tooltip = Tooltip(tooltipText)
            styleClass.addAll(Styles.FLAT, *styleClasses)

            val size = UIConstants.TOP_BAR_BUTTON_SIZE
            prefWidth = size
            minWidth = size
            maxWidth = size
            prefHeight = size
            minHeight = size
            maxHeight = size

            setOnAction { action() }
        }
    }

    private fun updateMaximizeButtonIcon(maximized: Boolean) {
        maximizeButton.graphic = FontIcon(if (maximized) Material2AL.FILTER_NONE else Material2AL.CROP_SQUARE)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWindowStateChanged(event: MainWindowMaximizedStateChanged) {
        Platform.runLater {
            updateMaximizeButtonIcon(event.isMaximized)
        }
    }
}