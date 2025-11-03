package io.github.frostzie.datapackide.screen

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.handlers.bars.BottomBarHandler
import io.github.frostzie.datapackide.handlers.bars.LeftBarHandler
import io.github.frostzie.datapackide.handlers.bars.top.TopBarHandler
import io.github.frostzie.datapackide.handlers.popup.file.FilePopupHandler
import io.github.frostzie.datapackide.handlers.popup.settings.SettingsHandler
import io.github.frostzie.datapackide.modules.bars.BottomBarModule
import io.github.frostzie.datapackide.modules.bars.LeftBarModule
import io.github.frostzie.datapackide.modules.bars.top.TopBarModule
import io.github.frostzie.datapackide.modules.popup.file.FilePopupModule
import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.handlers.popup.settings.ThemeHandler
import io.github.frostzie.datapackide.modules.popup.settings.ThemeModule
import io.github.frostzie.datapackide.screen.elements.bars.BottomBarView
import io.github.frostzie.datapackide.screen.elements.bars.LeftBarView
import io.github.frostzie.datapackide.screen.elements.bars.top.ToolBarMenu
import io.github.frostzie.datapackide.screen.elements.bars.top.TopBarView
import io.github.frostzie.datapackide.screen.elements.main.FileTreeView
import io.github.frostzie.datapackide.screen.elements.main.TextEditorView
import io.github.frostzie.datapackide.screen.elements.popup.settings.SettingsView
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.WindowResizer
import io.github.frostzie.datapackide.utils.dev.DebugManager
import javafx.scene.layout.Pane
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.WindowDrag
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.control.SplitPane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import io.github.frostzie.datapackide.settings.categories.ThemeConfig
import io.github.frostzie.datapackide.utils.ThemeUtils

class MainApplication {

    companion object {
        private val logger = LoggerProvider.getLogger("MainApplication")
        private var primaryStage: Stage? = null
        private var fxInitialized = false

        // UI Components
        private var topBarView: TopBarView? = null
        private var leftBarView: LeftBarView? = null
        private var fileTreeView: FileTreeView? = null
        private var bottomBarView: BottomBarView? = null
        private var settingsView: SettingsView? = null
        private var textEditorView: TextEditorView? = null
        private var contentArea: SplitPane? = null

        // Modules and Handlers
        private var toolBarMenu: ToolBarMenu? = null
        private var topBarModule: TopBarModule? = null
        private var topBarHandler: TopBarHandler? = null

        private var leftBarModule: LeftBarModule? = null
        private var leftBarHandler: LeftBarHandler? = null

        private var bottomBarModule: BottomBarModule? = null
        private var bottomBarHandler: BottomBarHandler? = null

        private var settingsModule: SettingsModule? = null
        private var settingsHandler: SettingsHandler? = null

        private var filePopupModule: FilePopupModule? = null
        private var filePopupHandler: FilePopupHandler? = null

        private var themeModule: ThemeModule? = null
        private var themeHandler: ThemeHandler? = null

        fun initializeJavaFX() {
            if (!fxInitialized) {
                System.setProperty("javafx.allowSystemPropertiesAccess", "true")
                System.setProperty("prism.allowhidpi", "false")

                try {
                    JavaFXInitializer.startup {
                        ThemeUtils.applyTheme(ThemeConfig.theme.get())
                        ThemeConfig.theme.addListener { _, _, newTheme -> ThemeUtils.applyTheme(newTheme) }
                        JavaFXInitializer.setImplicitExit(false)
                        fxInitialized = true
                        createMainWindow()
                        logger.info("JavaFX Platform initialized and main window pre-created!")
                    }
                } catch (e: IllegalStateException) {
                    fxInitialized = true
                    JavaFXInitializer.runLater {
                        JavaFXInitializer.setImplicitExit(false)
                        createMainWindow()
                        logger.info("JavaFX Platform was already initialized, main window pre-created!", e)
                    }
                } catch (e: Exception) {
                    logger.error("Failed to initialize JavaFX", e)
                }
            }
        }

        private fun createMainUI(stage: Stage): Pane {
            val root = BorderPane()
            root.styleClass.add("window") // Add CSS class for drop shadow
            stage.icons.add(Image("assets/datapack-ide/icon.png"))

            toolBarMenu = ToolBarMenu()
            topBarView = TopBarView(toolBarMenu!!)
            leftBarView = LeftBarView()
            textEditorView = TextEditorView()
            fileTreeView = FileTreeView()
            bottomBarView = BottomBarView()

            bottomBarModule = BottomBarModule()
            bottomBarHandler = BottomBarHandler(bottomBarModule!!)

            topBarModule = TopBarModule(stage, topBarView)
            topBarHandler = TopBarHandler(topBarModule!!)

            leftBarModule = LeftBarModule(stage)
            leftBarHandler = LeftBarHandler(leftBarModule!!)

            themeModule = ThemeModule()
            themeHandler = ThemeHandler(themeModule!!)

            settingsModule = SettingsModule(stage)
            settingsHandler = SettingsHandler(settingsModule!!)

            filePopupModule = FilePopupModule(stage)
            filePopupHandler = FilePopupHandler(filePopupModule!!)

            setupEventHandlers()

            contentArea = SplitPane().apply {
                items.addAll(fileTreeView, textEditorView)

                val defaultPosition = UIConstants.FILE_TREE_DEFAULT_WIDTH / (UIConstants.DEFAULT_WINDOW_WIDTH - UIConstants.LEFT_BAR_WIDTH)
                setDividerPosition(0, defaultPosition)

                SplitPane.setResizableWithParent(fileTreeView, false)
                SplitPane.setResizableWithParent(textEditorView, true)
            }

            val centerContent = HBox().apply {
                children.addAll(leftBarView, contentArea)
                HBox.setHgrow(contentArea, Priority.ALWAYS)
            }

            root.top = topBarView
            root.center = centerContent
            root.bottom = bottomBarView

            val rootStack = StackPane(root, toolBarMenu!!.modalPane)

            setupStageDimensions(stage, root)
            WindowDrag.makeDraggable(stage, topBarView!!)

            // The resizable wrapper should wrap the StackPane to allow resizing modal panes as well.
            val resizableWrapper = WindowResizer.install(stage, rootStack)
            DebugManager.initialize(resizableWrapper)

            return resizableWrapper
        }

        private fun setupStageDimensions(stage: Stage, root: BorderPane) {
            val minContentWidth = UIConstants.MIN_CONTENT_WIDTH
            val minContentHeight = UIConstants.MIN_CONTENT_HEIGHT
            val maxContentWidth = Double.MAX_VALUE
            val maxContentHeight = Double.MAX_VALUE

            val borderWidth = UIConstants.WINDOW_BORDER_WIDTH
            val topBarHeight = UIConstants.TOP_BAR_HEIGHT
            val statusBarHeight = UIConstants.BOTTOM_BAR_HEIGHT
            
            root.minWidth = minContentWidth + borderWidth
            root.maxWidth = maxContentWidth + borderWidth
            root.minHeight = minContentHeight + topBarHeight + statusBarHeight + borderWidth
            root.maxHeight = maxContentHeight + topBarHeight + statusBarHeight + borderWidth

            stage.minWidth = root.minWidth + UIConstants.STAGE_BORDER_WIDTH
            stage.minHeight = root.minHeight + UIConstants.STAGE_BORDER_WIDTH
            stage.maxWidth = root.maxWidth + UIConstants.STAGE_BORDER_WIDTH
            stage.maxHeight = root.maxHeight + UIConstants.STAGE_BORDER_WIDTH

            logger.debug("Stage dimensions set: min=${stage.minWidth}x${stage.minHeight}, max=${stage.maxWidth}x${stage.maxHeight}")
        }

        private fun setupEventHandlers() {
            // EventBus registrations - only for handlers, not views
            EventBus.register(topBarHandler!!)
            topBarView?.let { EventBus.register(it) }

            EventBus.register(leftBarHandler!!)
            leftBarView?.let { EventBus.register(it) }

            EventBus.register(bottomBarHandler!!)
            bottomBarView?.let { EventBus.register(it) }

            EventBus.register(settingsHandler!!)
            settingsView?.let { EventBus.register(it) }

            EventBus.register(filePopupHandler!!)

            EventBus.register(themeHandler!!)

            logger.debug("Event handlers initialized")
        }

        fun showMainWindow() {
            if (!fxInitialized) {
                initializeJavaFX()
                return
            }
            JavaFXInitializer.runLater {
                if (primaryStage == null) {
                    createMainWindow()
                }
                primaryStage?.show()
                primaryStage?.toFront()
                textEditorView?.requestFocus()
                logger.info("Main IDE Window shown!")
            }
        }

        private fun createMainWindow() {
            if (primaryStage != null) return

            try {
                val stage = Stage()
                stage.initStyle(StageStyle.UNDECORATED)

                val mainUI = createMainUI(stage)
                val scene = Scene(mainUI, UIConstants.DEFAULT_WINDOW_WIDTH, UIConstants.DEFAULT_WINDOW_HEIGHT)

                CSSManager.applyAllStyles(scene)
                themeModule?.scenes?.add(scene)
                stage.scene = scene
                stage.title = "DataPack IDE"
                stage.width = UIConstants.DEFAULT_WINDOW_WIDTH
                stage.height = UIConstants.DEFAULT_WINDOW_HEIGHT
                stage.isResizable = true
                stage.centerOnScreen()

                stage.setOnCloseRequest { e ->
                    e.consume()
                    hideMainWindow()
                }

                stage.focusedProperty().addListener { _, _, focused ->
                    fileTreeView?.viewModel?.setWindowFocused(focused)
                }

                primaryStage = stage
                logger.info("Main IDE Window created with ResizeHandler (hidden)!")
            } catch (e: Exception) {
                logger.error("Failed to create main window: ${e.message}", e)
            }
        }

        fun hideMainWindow() {
            JavaFXInitializer.runLater {
                primaryStage?.takeIf { it.isShowing }?.let {
                    it.hide()
                    logger.debug("Main IDE Window hidden via hideMainWindow()!")
                }
            }
        }

        fun toggleMainWindow() {
            if (!fxInitialized) {
                logger.info("JavaFX not initialized yet, initializing...")
                initializeJavaFX()
                return
            }
            Platform.runLater {
                if (primaryStage == null) {
                    createMainWindow()
                }
                primaryStage?.let { stage ->
                    if (stage.isShowing) {
                        logger.info("Window is showing, hiding it...")
                        stage.hide()
                        logger.info("Main IDE Window hidden!")
                    } else {
                        stage.show()
                        stage.toFront()
                        textEditorView?.requestFocus()
                        logger.info("Main IDE Window shown from hidden!")
                    }
                }
            }
        }
    }
}