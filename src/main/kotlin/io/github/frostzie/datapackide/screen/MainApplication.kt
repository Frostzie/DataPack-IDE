package io.github.frostzie.datapackide.screen

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.eventsOLD.EventHandlerSystem
import io.github.frostzie.datapackide.handlers.bars.top.TopBarHandler
import io.github.frostzie.datapackide.modules.bars.top.TopBarModule
import io.github.frostzie.datapackide.modules.popup.SettingsModule
import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.screen.elements.main.FileTreeView
import io.github.frostzie.datapackide.screen.elements.bars.LeftBarView
import io.github.frostzie.datapackide.screen.elements.bars.BottomBarView
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import io.github.frostzie.datapackide.screen.elements.bars.top.TopBarView
import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.ResizeHandler
import javafx.application.Platform
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.Scene
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
        private var textEditor: TextEditor? = null
        private var contentArea: HBox? = null

        // Action handler
        private var eventHandlerSystem: EventHandlerSystem? = null

        // New Modules and Handlers
        private var topBarModule: TopBarModule? = null
        private var topBarHandler: TopBarHandler? = null
        private var settingsModule: SettingsModule? = null

        fun initializeJavaFX() {
            if (!fxInitialized) {
                System.setProperty("javafx.allowSystemPropertiesAccess", "true")
                System.setProperty("prism.allowhidpi", "false")

                try {
                    JavaFXInitializer.startup {
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
            textEditor = TextEditor()
            fileTreeView = FileTreeView()
            bottomBarView = BottomBarView()

            eventHandlerSystem = EventHandlerSystem(textEditor, fileTreeView, bottomBarView, stage)

            topBarModule = TopBarModule(stage)
            topBarHandler = TopBarHandler(topBarModule!!)
            settingsModule = SettingsModule(stage)

            setupEventHandlers()
            setupTextEditorBindings()

            contentArea = HBox().apply {
                children.addAll(fileTreeView, textEditor)
                HBox.setHgrow(textEditor, Priority.ALWAYS)
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

            setupStageDimensions(stage, root)

            return root
        }

        private fun setupStageDimensions(stage: Stage, root: BorderPane) {
            val minContentWidth = 800.0
            val minContentHeight = 600.0
            val maxContentWidth = Double.MAX_VALUE
            val maxContentHeight = Double.MAX_VALUE

            val borderWidth = UIConstants.WINDOW_BORDER_WIDTH
            val topBarHeight = UIConstants.TOP_BAR_HEIGHT
            val statusBarHeight = UIConstants.STATUS_BAR_HEIGHT
            val leftSidebarWidth = UIConstants.LEFT_SIDEBAR_WIDTH
            
            root.minWidth = minContentWidth + borderWidth
            root.maxWidth = maxContentWidth + borderWidth
            root.minHeight = minContentHeight + topBarHeight + statusBarHeight + borderWidth
            root.maxHeight = maxContentHeight + topBarHeight + statusBarHeight + borderWidth

            stage.minWidth = root.minWidth + 4.0
            stage.minHeight = root.minHeight + 4.0
            stage.maxWidth = root.maxWidth + 4.0
            stage.maxHeight = root.maxHeight + 4.0

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
            topBarView?.let {
                EventBus.register(it)
                EventBus.register(it.windowControls)
            }

            // Old event system for remaining components
            eventHandlerSystem?.initialize()
            logger.debug("Event handlers initialized")
        }

        private fun setupTextEditorBindings() {
            textEditor?.let { editor ->
                bottomBarView?.let { status ->
                    editor.onCursorPositionChanged = { line, column ->
                        status.updateCursorPosition(line, column)
                    }
                    logger.debug("Text editor bindings set up with status bar")
                }
            }
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
                textEditor?.requestFocus()
                logger.info("Main IDE Window shown!")
            }
        }

        private fun createMainWindow() {
            if (primaryStage != null) return

            try {
                val stage = Stage()
                stage.initStyle(StageStyle.UNDECORATED)

                val mainUI = createMainUI(stage)
                val scene = Scene(mainUI, 1200.0, 800.0)

                stage.scene = scene
                stage.title = "DataPack IDE"
                stage.width = 1200.0
                stage.height = 800.0
                stage.isResizable = true
                stage.centerOnScreen()

                CSSManager.applyAllStyles(scene)
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
                        textEditor?.requestFocus()
                        logger.info("Main IDE Window shown!")
                    }
                }
            }
        }
    }
}