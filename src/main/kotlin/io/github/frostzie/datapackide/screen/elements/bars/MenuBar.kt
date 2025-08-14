package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class MenuBar : javafx.scene.control.MenuBar() {

    companion object {
        private val LOGGER = LoggerProvider.getLogger("MenuBar")
    }

    // Callbacks for menu actions
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
        createMenus()
        LOGGER.info("MenuBar initialized")
    }

    private fun createMenus() {
        menus.addAll(
            createFileMenu(),
            createEditMenu(),
            createViewMenu(),
            createDatapackMenu(),
            createHelpMenu()
        )
    }

    private fun createFileMenu(): Menu {
        val fileMenu = Menu("File")

        val newFileItem = MenuItem("New File").apply {
            accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onNewFile?.invoke()
                LOGGER.info("New File action triggered")
            }
        }

        val openFileItem = MenuItem("Open File...").apply {
            accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onOpenFile?.invoke()
                LOGGER.info("Open File action triggered")
            }
        }

        val saveFileItem = MenuItem("Save").apply {
            accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onSaveFile?.invoke()
                LOGGER.info("Save File action triggered")
            }
        }

        val saveAsFileItem = MenuItem("Save As...").apply {
            accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
            setOnAction {
                onSaveAsFile?.invoke()
                LOGGER.info("Save As action triggered")
            }
        }

        val closeFileItem = MenuItem("Close File").apply {
            accelerator = KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onCloseFile?.invoke()
                LOGGER.info("Close File action triggered")
            }
        }

        val exitItem = MenuItem("Exit").apply {
            accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onExit?.invoke()
                LOGGER.info("Exit action triggered")
            }
        }

        fileMenu.items.addAll(
            newFileItem,
            openFileItem,
            SeparatorMenuItem(),
            saveFileItem,
            saveAsFileItem,
            SeparatorMenuItem(),
            closeFileItem,
            SeparatorMenuItem(),
            exitItem
        )

        return fileMenu
    }

    private fun createEditMenu(): Menu {
        val editMenu = Menu("Edit")

        val undoItem = MenuItem("Undo").apply {
            accelerator = KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onUndo?.invoke()
                LOGGER.info("Undo action triggered")
            }
        }

        val redoItem = MenuItem("Redo").apply {
            accelerator = KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onRedo?.invoke()
                LOGGER.info("Redo action triggered")
            }
        }

        val cutItem = MenuItem("Cut").apply {
            accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onCut?.invoke()
                LOGGER.info("Cut action triggered")
            }
        }

        val copyItem = MenuItem("Copy").apply {
            accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onCopy?.invoke()
                LOGGER.info("Copy action triggered")
            }
        }

        val pasteItem = MenuItem("Paste").apply {
            accelerator = KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onPaste?.invoke()
                LOGGER.info("Paste action triggered")
            }
        }

        val findItem = MenuItem("Find").apply {
            accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onFind?.invoke()
                LOGGER.info("Find action triggered")
            }
        }

        val replaceItem = MenuItem("Replace").apply {
            accelerator = KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN)
            setOnAction {
                onReplace?.invoke()
                LOGGER.info("Replace action triggered")
            }
        }

        editMenu.items.addAll(
            undoItem,
            redoItem,
            SeparatorMenuItem(),
            cutItem,
            copyItem,
            pasteItem,
            SeparatorMenuItem(),
            findItem,
            replaceItem
        )

        return editMenu
    }

    private fun createViewMenu(): Menu {
        val viewMenu = Menu("View")

        val toggleSidebarItem = CheckMenuItem("Show Sidebar").apply {
            isSelected = true
            setOnAction {
                LOGGER.info("Toggle Sidebar: ${isSelected}")
            }
        }

        val toggleStatusBarItem = CheckMenuItem("Show Status Bar").apply {
            isSelected = true
            setOnAction {
                LOGGER.info("Toggle Status Bar: ${isSelected}")
            }
        }

        val themeMenu = Menu("Theme")
        val toggleGroup = ToggleGroup()

        val darkThemeItem = RadioMenuItem("Dark Theme").apply {
            //TODO: Implement dark theme
        }

        val lightThemeItem = RadioMenuItem("Light Theme").apply {
            //TODO: Implement light theme
        }

        themeMenu.items.addAll(darkThemeItem, lightThemeItem)

        viewMenu.items.addAll(
            toggleSidebarItem,
            toggleStatusBarItem,
            SeparatorMenuItem(),
            themeMenu
        )

        return viewMenu
    }

    private fun createDatapackMenu(): Menu {
        val datapackMenu = Menu("Datapack")

        val runItem = MenuItem("Run Datapack").apply {
            accelerator = KeyCodeCombination(KeyCode.F5)
            setOnAction {
                onRunDatapack?.invoke()
                LOGGER.info("Run Datapack action triggered")
            }
        }

        val validateItem = MenuItem("Validate Datapack").apply {
            accelerator = KeyCodeCombination(KeyCode.F7)
            setOnAction {
                onValidateDatapack?.invoke()
                LOGGER.info("Validate Datapack action triggered")
            }
        }

        val packageItem = MenuItem("Package Datapack").apply {
            accelerator = KeyCodeCombination(KeyCode.F9)
            setOnAction {
                onPackageDatapack?.invoke()
                LOGGER.info("Package Datapack action triggered")
            }
        }

        datapackMenu.items.addAll(
            runItem,
            validateItem,
            packageItem
        )

        return datapackMenu
    }

    private fun createHelpMenu(): Menu {
        val helpMenu = Menu("Help")

        val preferencesItem = MenuItem("Preferences").apply {
            setOnAction {
                onPreferences?.invoke()
                LOGGER.info("Preferences action triggered")
            }
        }

        val aboutItem = MenuItem("About DataPack IDE").apply {
            setOnAction {
                onAbout?.invoke()
                LOGGER.info("About action triggered")
            }
        }

        helpMenu.items.addAll(
            preferencesItem,
            SeparatorMenuItem(),
            aboutItem
        )

        return helpMenu
    }
}