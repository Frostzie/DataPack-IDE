package io.github.frostzie.datapackide.screen

import io.github.frostzie.datapackide.screen.elements.main.TextEditor
import io.github.frostzie.datapackide.screen.elements.main.FileTreeView
import io.github.frostzie.datapackide.screen.elements.bars.LeftSidebar
import io.github.frostzie.datapackide.screen.elements.bars.StatusBar
import io.github.frostzie.datapackide.screen.elements.bars.TitleBar
import io.github.frostzie.datapackide.screen.handlers.MenuActionHandler
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import io.github.frostzie.datapackide.utils.ResizeHandler
import javafx.application.Platform
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlin.system.exitProcess

class MainApplication {

    companion object {
        private val logger = LoggerProvider.getLogger("MainApplication")
        private var primaryStage: Stage? = null
        private var fxInitialized = false
        private var isStandaloneMode = false

        // UI Components
        private var titleBar: TitleBar? = null
        private var leftSidebar: LeftSidebar? = null
        private var fileTreeView: FileTreeView? = null
        private var statusBar: StatusBar? = null
        private var textEditor: TextEditor? = null
        private var contentArea: HBox? = null

        // Action handler
        private var menuActionHandler: MenuActionHandler? = null

        fun initializeJavaFX() {
            if (!fxInitialized) {
                System.setProperty("javafx.allowSystemPropertiesAccess", "true")
                System.setProperty("prism.allowhidpi", "false")

                try {
                    if (isStandaloneMode) {
                        JavaFXInitializer.setImplicitExit(false)
                        fxInitialized = true
                        createMainWindow()
                        logger.info("JavaFX initialized in standalone mode!")
                    } else {
                        JavaFXInitializer.startup {
                            JavaFXInitializer.setImplicitExit(false)
                            fxInitialized = true
                            createMainWindow()
                            logger.info("JavaFX Platform initialized and main window pre-created!")
                        }
                    }
                } catch (e: IllegalStateException) {
                    fxInitialized = true
                    JavaFXInitializer.runLater {
                        JavaFXInitializer.setImplicitExit(!isStandaloneMode)
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

            titleBar = TitleBar(stage, isStandaloneMode)
            leftSidebar = LeftSidebar()
            fileTreeView = FileTreeView()
            statusBar = StatusBar()
            textEditor = TextEditor()

            menuActionHandler = MenuActionHandler(textEditor, statusBar, stage)

            setupTitleBarCallbacks()
            setupTextEditorBindings()
            setupEventHandlers()

            contentArea = HBox().apply {
                children.addAll(fileTreeView, textEditor)
                HBox.setHgrow(textEditor, Priority.ALWAYS)
                HBox.setHgrow(fileTreeView, Priority.NEVER) // Prevent HBox from resizing FileTree
                spacing = 0.0
                prefHeight = Region.USE_COMPUTED_SIZE
                maxHeight = Double.MAX_VALUE
            }

            val mainContent = VBox().apply {
                children.addAll(titleBar, contentArea)
                VBox.setVgrow(contentArea, Priority.ALWAYS)
            }

            root.left = leftSidebar
            root.center = mainContent
            root.bottom = statusBar

            BorderPane.setAlignment(mainContent, javafx.geometry.Pos.TOP_LEFT)
            setupStageDimensions(stage, root)

            return root
        }

        private fun setupStageDimensions(stage: Stage, root: BorderPane) {
            val minContentWidth = 800.0
            val minContentHeight = 600.0
            val maxContentWidth = Double.MAX_VALUE
            val maxContentHeight = Double.MAX_VALUE

            val borderWidth = 2.0
            val titleBarHeight = 32.0
            val statusBarHeight = 24.0
            val leftSidebarWidth = 40.0

            root.minWidth = minContentWidth + borderWidth
            root.maxWidth = maxContentWidth + borderWidth
            root.minHeight = minContentHeight + titleBarHeight + statusBarHeight + borderWidth
            root.maxHeight = maxContentHeight + titleBarHeight + statusBarHeight + borderWidth

            stage.minWidth = root.minWidth + 4.0
            stage.minHeight = root.minHeight + 4.0
            stage.maxWidth = root.maxWidth + 4.0
            stage.maxHeight = root.maxHeight + 4.0

            logger.debug("Stage dimensions set: min=${stage.minWidth}x${stage.minHeight}, max=${stage.maxWidth}x${stage.maxHeight}")
        }

        private fun setupWindowResizing(stage: Stage) {
            if (stage.style == StageStyle.UNDECORATED) {
                ResizeHandler.install(stage, 32.0, 7.0, 0.0) // titlebarHeight=32, pullEdgeDepth=7, indentation=0
                logger.debug("ResizeHandler installed for undecorated window")
            } else {
                logger.debug("ResizeHandler not installed - stage is decorated")
            }
        }

        private fun setupEventHandlers() {
            io.github.frostzie.datapackide.events.EventBus.register<io.github.frostzie.datapackide.events.FileOpenEvent> { event ->
                logger.info("FileOpenEvent received: ${event.filePath}")
                try {
                    val content = event.filePath.toFile().readText()
                    textEditor?.setText(content, event.filePath.toString())
                    logger.info("File opened successfully: ${event.filePath.fileName}")
                } catch (e: Exception) {
                    logger.error("Failed to open file: ${event.filePath}", e)
                }
            }
        }

        private fun setupTextEditorBindings() {
            textEditor?.let { editor ->
                statusBar?.let { status ->
                    editor.onCursorPositionChanged = { line, column ->
                        status.updateCursorPosition(line, column)
                    }
                    logger.debug("Text editor bindings set up with status bar")
                }
            }
        }

        private fun setupTitleBarCallbacks() {
            titleBar?.let { bar ->
                menuActionHandler?.let { handler ->
                    // File menu
                    bar.onNewFile = { handler.createNewFile() }
                    bar.onOpenFile = { handler.openFile() }
                    bar.onSaveFile = { handler.saveCurrentFile() }
                    bar.onSaveAsFile = { handler.saveAsFile() }
                    bar.onCloseFile = { handler.closeCurrentFile() }
                    bar.onExit = { exitApplication() }

                    // Edit menu
                    bar.onUndo = { handler.performUndo() }
                    bar.onRedo = { handler.performRedo() }
                    bar.onCut = { handler.performCut() }
                    bar.onCopy = { handler.performCopy() }
                    bar.onPaste = { handler.performPaste() }
                    bar.onFind = { handler.showFindDialog() }
                    bar.onReplace = { handler.showReplaceDialog() }

                    // Datapack menu
                    bar.onRunDatapack = { handler.runDatapack() }
                    bar.onValidateDatapack = { handler.validateDatapack() }
                    bar.onPackageDatapack = { handler.packageDatapack() }

                    // Help menu
                    bar.onPreferences = { handler.showPreferences() }
                    bar.onAbout = { handler.showAbout() }
                }
            }
        }

        private fun exitApplication() {
            if (isStandaloneMode) {
                Platform.exit()
                exitProcess(0)
            } else {
                hideMainWindow()
                logger.info("Application exit requested")
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

                loadStylesheets(scene)

                stage.scene = scene
                stage.title = "DataPack IDE ${if (isStandaloneMode) "(Standalone)" else ""}"
                stage.width = 1200.0
                stage.height = 800.0
                stage.isResizable = true
                stage.centerOnScreen()

                setupWindowResizing(stage)

                stage.setOnCloseRequest { e ->
                    if (isStandaloneMode) {
                        Platform.exit()
                        exitProcess(0)
                    } else {
                        e.consume()
                        logger.info("Close button pressed, hiding window...")
                        JavaFXInitializer.runLater {
                            stage.hide()
                            logger.info("Window hidden via close button!")
                        }
                    }
                }

                primaryStage = stage
                logger.info("Main IDE Window created with ResizeHandler (hidden)!")
            } catch (e: Exception) {
                logger.error("Failed to create main window: ${e.message}", e)
            }
        }

        private fun loadStylesheets(scene: Scene) {
            val cssFiles = listOf(
                "/assets/datapack-ide/themes/MenuBar.css",
                "/assets/datapack-ide/themes/TitleBar.css",
                "/assets/datapack-ide/themes/FileTree.css",
                "/assets/datapack-ide/themes/Window.css"
            )

            try {
                cssFiles.forEach { cssPath ->
                    val cssUrl = MainApplication::class.java.getResource(cssPath)
                    if (cssUrl != null) {
                        scene.stylesheets.add(cssUrl.toExternalForm())
                        logger.debug("Loaded CSS: $cssPath")
                    } else {
                        logger.warn("CSS file not found: $cssPath")
                    }
                }
                logger.info("Custom themes loaded successfully")
            } catch (e: Exception) {
                logger.warn("Could not load custom themes: ${e.message}, using default styling")
            }
        }

        fun hideMainWindow() {
            JavaFXInitializer.runLater {
                primaryStage?.takeIf { it.isShowing }?.let {
                    it.hide()
                    logger.info("Main IDE Window hidden via hideMainWindow()!")
                }
            }
        }

        fun isWindowVisible(): Boolean {
            return primaryStage?.isShowing == true
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