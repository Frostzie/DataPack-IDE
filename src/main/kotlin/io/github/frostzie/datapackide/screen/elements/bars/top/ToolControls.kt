package io.github.frostzie.datapackide.screen.elements.bars.top

import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MenuActionEvent
import io.github.frostzie.datapackide.events.MenuCategory
import io.github.frostzie.datapackide.events.MenuAction
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class ToolControls : MenuBar() {

    companion object {
        private val logger = LoggerProvider.getLogger("ToolControls")
    }

    init {
        styleClass.add("main-menu-bar")
        createMenus()
        logger.info("ToolControls initialized with event system")
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
                    EventBus.post(MenuActionEvent(MenuCategory.FILE, MenuAction.NEW_FILE))
                },
                createMenuItem("Open File...", "menu-item-open", KeyCode.O, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.FILE, MenuAction.OPEN_FILE))
                },
                SeparatorMenuItem(),
                createMenuItem("Save", "menu-item-save", KeyCode.S, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.FILE, MenuAction.SAVE_FILE))
                },
                createMenuItem("Save As...", "menu-item-saveas", KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.FILE, MenuAction.SAVE_AS_FILE))
                },
                SeparatorMenuItem(),
                createMenuItem("Close File", "menu-item-close", KeyCode.W, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.FILE, MenuAction.CLOSE_FILE))
                },
                SeparatorMenuItem(),
                createMenuItem("Exit", "menu-item-exit", KeyCode.Q, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.FILE, MenuAction.EXIT))
                }
            )
        }
    }

    private fun createEditMenu(): Menu {
        return Menu("Edit").apply {
            styleClass.add("menu-edit")
            items.addAll(
                createMenuItem("Undo", "menu-item-undo", KeyCode.Z, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.UNDO))
                },
                createMenuItem("Redo", "menu-item-redo", KeyCode.Y, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.REDO))
                },
                SeparatorMenuItem(),
                createMenuItem("Cut", "menu-item-cut", KeyCode.X, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.CUT))
                },
                createMenuItem("Copy", "menu-item-copy", KeyCode.C, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.COPY))
                },
                createMenuItem("Paste", "menu-item-paste", KeyCode.V, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.PASTE))
                },
                SeparatorMenuItem(),
                createMenuItem("Find", "menu-item-find", KeyCode.F, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.FIND))
                },
                createMenuItem("Replace", "menu-item-replace", KeyCode.R, KeyCombination.CONTROL_DOWN) {
                    EventBus.post(MenuActionEvent(MenuCategory.EDIT, MenuAction.REPLACE))
                }
            )
        }
    }

    private fun createDatapackMenu(): Menu {
        return Menu("Datapack").apply {
            styleClass.add("menu-datapack")
            items.addAll(
                createMenuItem("Run Datapack", "menu-item-run", KeyCode.F5) {
                    EventBus.post(MenuActionEvent(MenuCategory.DATAPACK, MenuAction.RELOAD_DATAPACKS))
                },
                createMenuItem("Validate Datapack", "menu-item-validate", KeyCode.F7) {
                    EventBus.post(MenuActionEvent(MenuCategory.DATAPACK, MenuAction.VALIDATE_DATAPACK))
                },
                createMenuItem("Package Datapack", "menu-item-package", KeyCode.F9) {
                    EventBus.post(MenuActionEvent(MenuCategory.DATAPACK, MenuAction.PACKAGE_DATAPACK))
                }
            )
        }
    }

    private fun createHelpMenu(): Menu {
        return Menu("Help").apply {
            styleClass.add("menu-help")
            items.addAll(
                createMenuItem("Preferences", "menu-item-preferences") {
                    EventBus.post(MenuActionEvent(MenuCategory.HELP, MenuAction.PREFERENCES))
                },
                SeparatorMenuItem(),
                createMenuItem("About DataPack IDE", "menu-item-about") {
                    EventBus.post(MenuActionEvent(MenuCategory.HELP, MenuAction.ABOUT))
                }
            )
        }
    }
}