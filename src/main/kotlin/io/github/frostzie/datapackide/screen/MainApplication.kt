package io.github.frostzie.datapackide.screen

import io.github.frostzie.datapackide.screen.elements.bars.LeftSidebar
import io.github.frostzie.datapackide.screen.elements.bars.MenuBar
import io.github.frostzie.datapackide.screen.elements.bars.StatusBar
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.JavaFXInitializer
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.slf4j.Logger
import kotlin.system.exitProcess

//TODO: After adding editing and file explorer window move to use css just like everything else.
//TODO: For text editor use: https://github.com/FXMisc/RichTextFX
//TODO: File explorer not sure if any dependencies needed or make fully ourselves
//TODO: Add move explanation to css files since they later will be used as custom theme import examples!

class MainApplication {

    companion object {
        private val LOGGER: Logger = LoggerProvider.getLogger("MainApplication")
        private var primaryStage: Stage? = null
        private var fxInitialized = false
        private var isStandaloneMode = false //TODO: Make a standalone version without minecraft for testing windows

        // UI Components
        private var menuBar: MenuBar? = null
        private var leftSidebar: LeftSidebar? = null
        private var statusBar: StatusBar? = null

        fun initializeJavaFX() {
            if (!fxInitialized) {
                System.setProperty("javafx.allowSystemPropertiesAccess", "true")
                System.setProperty("prism.allowhidpi", "false")

                try {
                    if (isStandaloneMode) {
                        JavaFXInitializer.setImplicitExit(false)
                        fxInitialized = true
                        createMainWindow()
                        LOGGER.info("JavaFX initialized in standalone mode!")
                    } else {
                        JavaFXInitializer.startup {
                            JavaFXInitializer.setImplicitExit(false)
                            fxInitialized = true
                            createMainWindow()
                            LOGGER.info("JavaFX Platform initialized and main window pre-created!")
                        }
                    }
                } catch (e: IllegalStateException) {
                    fxInitialized = true
                    JavaFXInitializer.runLater {
                        JavaFXInitializer.setImplicitExit(!isStandaloneMode)
                        createMainWindow()
                        LOGGER.info("JavaFX Platform was already initialized, main window pre-created!")
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to initialize JavaFX", e)
                }
            }
        }

        private fun createMainUI(): BorderPane {
            val root = BorderPane()

            // Initialize components
            menuBar = MenuBar().also { setupMenuBarCallbacks(it) }
            leftSidebar = LeftSidebar()
            statusBar = StatusBar()

            val placeholder = Label("File Explorer and Text Editor currently not implemented!")
            val mainContent = VBox().apply {
                children.addAll(menuBar, placeholder)
                VBox.setVgrow(placeholder, Priority.ALWAYS)
            }

            // Layout components
            root.left = leftSidebar
            root.center = mainContent
            root.bottom = statusBar

            return root
        }

        private fun setupMenuBarCallbacks(menuBar: MenuBar) {
            menuBar.onNewFile = { createNewFile() }
            menuBar.onOpenFile = { openFile() }
            menuBar.onSaveFile = { saveCurrentFile() }
            menuBar.onSaveAsFile = { saveAsFile() }
            menuBar.onCloseFile = { closeCurrentFile() }
            menuBar.onExit = { exitApplication() }

            menuBar.onUndo = { performUndo() }
            menuBar.onRedo = { performRedo() }
            menuBar.onCut = { performCut() }
            menuBar.onCopy = { performCopy() }
            menuBar.onPaste = { performPaste() }
            menuBar.onFind = { showFindDialog() }
            menuBar.onReplace = { showReplaceDialog() }

            menuBar.onRunDatapack = { runDatapack() }
            menuBar.onValidateDatapack = { validateDatapack() }
            menuBar.onPackageDatapack = { packageDatapack() }

            menuBar.onPreferences = { showPreferences() }
        }

        // Menu action implementations replaced with placeholders since editor is removed
        private fun createNewFile() {
            LOGGER.info("createNewFile() disabled as Editor is removed")
        }

        private fun openFile() {
            LOGGER.info("openFile() disabled as Editor is removed")
        }

        private fun saveCurrentFile() {
            LOGGER.info("saveCurrentFile() disabled as Editor is removed")
        }

        private fun saveAsFile() {
            LOGGER.info("saveAsFile() disabled as Editor is removed")
        }

        private fun closeCurrentFile() {
            LOGGER.info("closeCurrentFile() disabled as Editor is removed")
        }

        private fun exitApplication() {
            if (isStandaloneMode) {
                Platform.exit()
                exitProcess(0)
            } else {
                hideMainWindow()
                LOGGER.info("Application exit requested")
            }
        }

        private fun performUndo() {
        }

        private fun performRedo() {
        }

        private fun performCut() {
        }

        private fun performCopy() {
        }

        private fun performPaste() {
        }

        private fun showFindDialog() {
        }

        private fun showReplaceDialog() {
        }

        private fun runDatapack() {
        }

        private fun validateDatapack() {
        }

        private fun packageDatapack() {
        }

        private fun showPreferences() {
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
                LOGGER.info("Main IDE Window shown!")
            }
        }

        private fun createMainWindow() {
            if (primaryStage != null) return

            try {
                val stage = Stage()

                // Create UI components
                val mainUI = createMainUI()
                val scene = Scene(mainUI, 1000.0, 600.0)

                try {
                    val cssUrl = MainApplication::class.java.getResource("/assets/datapack-ide/themes/MenuBar.css")
                    if (cssUrl != null) {
                        scene.stylesheets.add(cssUrl.toExternalForm())
                        LOGGER.info("Custom theme loaded successfully")
                    } else {
                        LOGGER.warn("Custom theme file not found, using default styling")
                    }
                } catch (e: Exception) {
                    LOGGER.warn("Could not load custom theme: ${e.message}, using default styling")
                }

                stage.scene = scene
                stage.title = "DataPack IDE ${if (isStandaloneMode) "(Standalone)" else ""}"
                stage.initStyle(StageStyle.DECORATED)
                stage.width = 1000.0
                stage.height = 600.0
                stage.centerOnScreen()

                stage.setOnCloseRequest { e ->
                    if (isStandaloneMode) {
                        Platform.exit()
                        exitProcess(0)
                    } else {
                        e.consume()
                        LOGGER.info("Close button pressed, hiding window...")
                        JavaFXInitializer.runLater {
                            stage.hide()
                            LOGGER.info("Window hidden via close button!")
                        }
                    }
                }

                primaryStage = stage
                LOGGER.info("Main IDE Window created (hidden)!")
            } catch (e: Exception) {
                LOGGER.error("Failed to create main window: ${e.message}", e)
            }
        }

        fun hideMainWindow() {
            JavaFXInitializer.runLater {
                primaryStage?.takeIf { it.isShowing }?.let {
                    it.hide()
                    LOGGER.info("Main IDE Window hidden via hideMainWindow()!")
                }
            }
        }

        fun isWindowVisible(): Boolean {
            return primaryStage?.isShowing == true
        }

        fun toggleMainWindow() {
            if (!fxInitialized) {
                LOGGER.info("JavaFX not initialized yet, initializing...")
                initializeJavaFX()
                return
            }
            Platform.runLater {
                if (primaryStage == null) {
                    createMainWindow()
                }
                primaryStage?.let { stage ->
                    if (stage.isShowing) {
                        LOGGER.info("Window is showing, hiding it...")
                        stage.hide()
                        LOGGER.info("Main IDE Window hidden!")
                    } else {
                        stage.show()
                        stage.toFront()
                        LOGGER.info("Main IDE Window shown!")
                    }
                }
            }
        }
    }
}