package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.events.AboutMod
import io.github.frostzie.datapackide.events.ChooseDirectory
import io.github.frostzie.datapackide.events.EditorCloseTab
import io.github.frostzie.datapackide.events.EditorCopy
import io.github.frostzie.datapackide.events.EditorCut
import io.github.frostzie.datapackide.events.EditorPaste
import io.github.frostzie.datapackide.events.EditorRedo
import io.github.frostzie.datapackide.events.EditorUndo
import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.ExportDatapack
import io.github.frostzie.datapackide.events.NewFile
import io.github.frostzie.datapackide.events.ReloadDatapack
import io.github.frostzie.datapackide.events.SaveAsFile
import io.github.frostzie.datapackide.events.SaveFile
import io.github.frostzie.datapackide.events.SettingsWindowOpen
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class MenuControls : MenuBar() {

    companion object {
        private val logger = LoggerProvider.getLogger("MenuControls")
    }

    init {
        styleClass.add("main-menu-bar")
        createMenus()
        logger.info("MenuControls initialized with event system")
    }

    private fun createMenus() {
        menus.addAll(
            createFileMenu(),
            createEditMenu(),
            createDatapackMenu(),
            createHelpMenu()
        )
    }

    private fun createMenuItem(
        text: String,
        styleClassName: String,
        keyCode: KeyCode? = null,
        vararg modifiers: KeyCombination.Modifier,
        action: () -> Unit
    ): MenuItem {
        return MenuItem(text).apply {
            styleClass.add(styleClassName)
            if (keyCode != null) {
                accelerator = KeyCodeCombination(keyCode, *modifiers)
            }
            setOnAction { action() }
        }
    }

    private fun createFileMenu(): Menu {
        return Menu("File").apply {
            styleClass.add("menu-file")
            items.addAll(
                createMenuItem("New File", "menu-item-new", KeyCode.N, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(NewFile())
                },
                createMenuItem("Open File...", "menu-item-open", KeyCode.O, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(ChooseDirectory())
                },
                SeparatorMenuItem(),
                createMenuItem("Save", "menu-item-save", KeyCode.S, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(SaveFile())
                },
                createMenuItem("Save As...", "menu-item-saveas", KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN) {
                    EventBus.post(SaveAsFile())
                },
                SeparatorMenuItem(),
                createMenuItem("Close File", "menu-item-close", KeyCode.W, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(EditorCloseTab())
                }
            )
        }
    }

    private fun createEditMenu(): Menu {
        return Menu("Edit").apply {
            styleClass.add("menu-edit")
            items.addAll(
                createMenuItem("Undo", "menu-item-undo", KeyCode.Z, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(EditorUndo())
                },
                createMenuItem("Redo", "menu-item-redo", KeyCode.Y, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(EditorRedo())
                },
                SeparatorMenuItem(),
                createMenuItem("Cut", "menu-item-cut", KeyCode.X, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(EditorCut())
                },
                createMenuItem("Copy", "menu-item-copy", KeyCode.C, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(EditorCopy())
                },
                createMenuItem("Paste", "menu-item-paste", KeyCode.V, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(EditorPaste())
                }
                //TODO: Find + Replace functionality
            )
        }
    }

    private fun createDatapackMenu(): Menu {
        return Menu("Datapack").apply {
            styleClass.add("menu-datapack")
            items.addAll(
                createMenuItem("Run Datapack", "menu-item-run") {
                    EventBus.post(ReloadDatapack())
                },
                createMenuItem("Package Datapack", "menu-item-package") {
                    EventBus.post(ExportDatapack())
                }
            )
        }
    }

    private fun createHelpMenu(): Menu {
        return Menu("Help").apply {
            styleClass.add("menu-help")
            items.addAll(
                createMenuItem("Preferences...", "menu-item-preferences") {
                    EventBus.post(SettingsWindowOpen())
                },
                SeparatorMenuItem(),
                createMenuItem("About DataPack IDE", "menu-item-about") {
                    EventBus.post(AboutMod())
                }
            )
        }
    }
}