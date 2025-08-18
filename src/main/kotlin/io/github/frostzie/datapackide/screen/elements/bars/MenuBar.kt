package io.github.frostzie.datapackide.screen.elements.bars

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class MenuBar : javafx.scene.control.MenuBar() {

    companion object {
        private val logger = LoggerProvider.getLogger("MenuBar")
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
        styleClass.add("main-menu-bar")
        stylesheets.add(javaClass.getResource("/assets/datapack-ide/themes/MenuBar.css")?.toExternalForm())
        createMenus()
        logger.info("MenuBar initialized")
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
                createMenuItem("New File", "menu-item-new", KeyCode.N, KeyCombination.CONTROL_DOWN) { onNewFile?.invoke() },
                createMenuItem("Open File...", "menu-item-open", KeyCode.O, KeyCombination.CONTROL_DOWN) { onOpenFile?.invoke() },
                SeparatorMenuItem(),
                createMenuItem("Save", "menu-item-save", KeyCode.S, KeyCombination.CONTROL_DOWN) { onSaveFile?.invoke() },
                createMenuItem("Save As...", "menu-item-saveas", KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN) { onSaveAsFile?.invoke() },
                SeparatorMenuItem(),
                createMenuItem("Close File", "menu-item-close", KeyCode.W, KeyCombination.CONTROL_DOWN) { onCloseFile?.invoke() },
                SeparatorMenuItem(),
                createMenuItem("Exit", "menu-item-exit", KeyCode.Q, KeyCombination.CONTROL_DOWN) { onExit?.invoke() }
            )
        }
    }

    private fun createEditMenu(): Menu {
        return Menu("Edit").apply {
            styleClass.add("menu-edit")
            items.addAll(
                createMenuItem("Undo", "menu-item-undo", KeyCode.Z, KeyCombination.CONTROL_DOWN) { onUndo?.invoke() },
                createMenuItem("Redo", "menu-item-redo", KeyCode.Y, KeyCombination.CONTROL_DOWN) { onRedo?.invoke() },
                SeparatorMenuItem(),
                createMenuItem("Cut", "menu-item-cut", KeyCode.X, KeyCombination.CONTROL_DOWN) { onCut?.invoke() },
                createMenuItem("Copy", "menu-item-copy", KeyCode.C, KeyCombination.CONTROL_DOWN) { onCopy?.invoke() },
                createMenuItem("Paste", "menu-item-paste", KeyCode.V, KeyCombination.CONTROL_DOWN) { onPaste?.invoke() },
                SeparatorMenuItem(),
                createMenuItem("Find", "menu-item-find", KeyCode.F, KeyCombination.CONTROL_DOWN) { onFind?.invoke() },
                createMenuItem("Replace", "menu-item-replace", KeyCode.R, KeyCombination.CONTROL_DOWN) { onReplace?.invoke() }
            )
        }
    }

    private fun createDatapackMenu(): Menu {
        return Menu("Datapack").apply {
            styleClass.add("menu-datapack")
            items.addAll(
                createMenuItem("Run Datapack", "menu-item-run", KeyCode.F5) { onRunDatapack?.invoke() },
                createMenuItem("Validate Datapack", "menu-item-validate", KeyCode.F7) { onValidateDatapack?.invoke() },
                createMenuItem("Package Datapack", "menu-item-package", KeyCode.F9) { onPackageDatapack?.invoke() }
            )
        }
    }

    private fun createHelpMenu(): Menu {
        return Menu("Help").apply {
            styleClass.add("menu-help")
            items.addAll(
                createMenuItem("Preferences", "menu-item-preferences") { onPreferences?.invoke() },
                SeparatorMenuItem(),
                createMenuItem("About DataPack IDE", "menu-item-about") { onAbout?.invoke() }
            )
        }
    }
}
