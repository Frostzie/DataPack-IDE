package io.github.frostzie.datapackide.screen

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.handlers.bars.BottomBarHandler
import io.github.frostzie.datapackide.handlers.bars.LeftBarHandler
import io.github.frostzie.datapackide.handlers.bars.top.TopBarHandler
import io.github.frostzie.datapackide.handlers.main.TextEditorHandler
import io.github.frostzie.datapackide.handlers.popup.settings.SettingsHandler
import io.github.frostzie.datapackide.modules.bars.BottomBarModule
import io.github.frostzie.datapackide.modules.bars.LeftBarModule
import io.github.frostzie.datapackide.modules.bars.top.TopBarModule
import io.github.frostzie.datapackide.modules.main.TextEditorModule
import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.handlers.popup.settings.ThemeHandler
import io.github.frostzie.datapackide.modules.popup.settings.ThemeModule
import io.github.frostzie.datapackide.screen.elements.bars.BottomBarView
import io.github.frostzie.datapackide.screen.elements.bars.LeftBarView
import io.github.frostzie.datapackide.screen.elements.bars.top.TopBarView
import io.github.frostzie.datapackide.screen.elements.main.FileTreeView
import io.github.frostzie.datapackide.screen.elements.main.TextEditorView
import io.github.frostzie.datapackide.screen.elements.popup.settings.SettingsView
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.ResizeHandler
import atlantafx.base.theme.PrimerDark
import io.github.frostzie.datapackide.settings.categories.AdvancedConfig
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.Stage
import javafx.stage.StageStyle

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
        private var contentArea: HBox? = null

        // New Modules and Handlers
        private var topBarModule: TopBarModule? = null
        private var topBarHandler: TopBarHandler? = null

        private var textEditorModule: TextEditorModule? = null
        private var textEditorHandler: TextEditorHandler? = null

        private var leftBarModule: LeftBarModule? = null
        private var leftBarHandler: LeftBarHandler? = null

        private var bottomBarModule: BottomBarModule? = null
        private var bottomBarHandler: BottomBarHandler? = null

        private var settingsModule: SettingsModule? = null
        private var settingsHandler: SettingsHandler? = null

        private var themeModule: ThemeModule? = null
        private var themeHandler: ThemeHandler? = null

        fun initializeJavaFX() {
            if (!fxInitialized) {
                System.setProperty("javafx.allowSystemPropertiesAccess", "true")
                System.setProperty("prism.allowhidpi", "false")

                try {
                    JavaFXInitializer.startup {
                        Application.setUserAgentStylesheet(PrimerDark().userAgentStylesheet)
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
                        logger.info("JavaFX Platform was already initialized, main window pre-created!")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to initialize JavaFX", e)
                }
            }
        }

        private fun createMainUI(stage: Stage): BorderPane {
            val root = BorderPane()
            root.styleClass.add("window") // Add CSS class for drop shadow

            topBarView = TopBarView()
            leftBarView = LeftBarView()
            textEditorView = TextEditorView()
            fileTreeView = FileTreeView()

            bottomBarModule = BottomBarModule()
            bottomBarHandler = BottomBarHandler(bottomBarModule!!)

            topBarModule = TopBarModule(stage, topBarView)
            topBarHandler = TopBarHandler(topBarModule!!)

            textEditorModule = TextEditorModule(textEditorView!!) //TODO: Change to actual module when moving View -> Module
            textEditorHandler = TextEditorHandler(textEditorModule!!)

            leftBarModule = LeftBarModule(stage)
            leftBarHandler = LeftBarHandler(leftBarModule!!)

            themeModule = ThemeModule()
            themeHandler = ThemeHandler(themeModule!!)

            settingsModule = SettingsModule(stage, themeModule!!)
            settingsHandler = SettingsHandler(settingsModule!!)

            setupEventHandlers()

            contentArea = HBox().apply {
                children.addAll(fileTreeView, textEditorView)
                HBox.setHgrow(textEditorView, Priority.ALWAYS)
                HBox.setHgrow(fileTreeView, Priority.NEVER) // Prevent HBox from resizing FileTree
                spacing = 0.0
                prefHeight = Region.USE_COMPUTED_SIZE
                maxHeight = Double.MAX_VALUE
            }

            val centerContent = HBox().apply {
                children.addAll(leftBarView, contentArea)
                HBox.setHgrow(contentArea, Priority.ALWAYS)
            }

            root.top = topBarView
            root.center = centerContent
            root.bottom = bottomBarView

            //TODO: remove / move
            AdvancedConfig.debugLayoutBounds.addListener { _, _, newValue ->
                if (newValue) {
                    root.styleClass.add("debug-layout")
                } else {
                    root.styleClass.remove("debug-layout")
                }
            }

            setupStageDimensions(stage, root)

            return root
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

        private fun setupWindowResizing(stage: Stage) {
            if (stage.style == StageStyle.UNDECORATED) {
                ResizeHandler.install(
                    stage,
                    UIConstants.TOP_BAR_HEIGHT,
                    UIConstants.WINDOW_RESIZE_BORDER_DEPTH,
                    UIConstants.WINDOW_SHADOW_INDENTATION
                )
                logger.debug("ResizeHandler installed for undecorated window")
            } else {
                logger.debug("ResizeHandler not installed - stage is decorated")
            }
        }

        private fun setupEventHandlers() {
            // New event bus registrations
            EventBus.register(topBarHandler!!)
            topBarView?.let { EventBus.register(it) }

            EventBus.register(textEditorHandler!!)
            textEditorView?.let { EventBus.register(it) }

            EventBus.register(leftBarHandler!!)
            leftBarView?.let { EventBus.register(it) }

            EventBus.register(bottomBarHandler!!)
            bottomBarView?.let { EventBus.register(it) }

            EventBus.register(settingsHandler!!)
            settingsView?.let { EventBus.register(it) }
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

                setupWindowResizing(stage)

                stage.setOnCloseRequest { e ->
                    e.consume()
                    hideMainWindow()
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
                        logger.info("Main IDE Window shown!")
                    }
                }
            }
        }
    }
}