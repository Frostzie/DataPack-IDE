package io.github.frostzie.datapackide.screen.elements.bars.top

import atlantafx.base.controls.ModalPane
import io.github.frostzie.datapackide.events.ChooseDirectory
import io.github.frostzie.datapackide.events.DiscordLink
import io.github.frostzie.datapackide.events.EditorCopy
import io.github.frostzie.datapackide.events.EditorCut
import io.github.frostzie.datapackide.events.EditorFind
import io.github.frostzie.datapackide.events.EditorRedo
import io.github.frostzie.datapackide.events.EditorSelectAll
import io.github.frostzie.datapackide.events.EditorUndo
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.GitHubLink
import io.github.frostzie.datapackide.events.OpenDatapackFolder
import io.github.frostzie.datapackide.events.ReloadDatapack
import io.github.frostzie.datapackide.events.ReportBugLink
import io.github.frostzie.datapackide.events.SaveAllFiles
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane

class ToolBarMenu {
    private val logger = LoggerProvider.getLogger("ToolBarMenu")
    val modalPane: ModalPane = ModalPane()
    val dialogContent: BorderPane

    init {
        modalPane.persistent = false
        dialogContent = createDialogContent()

        StackPane.setAlignment(dialogContent, Pos.TOP_LEFT)
        StackPane.setMargin(dialogContent, Insets.EMPTY)
    }

    fun show() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater { modalPane.show(dialogContent) }
        } else {
            modalPane.show(dialogContent)
        }
    }

    private fun createDialogContent(): BorderPane {
        val dialogContent = BorderPane()

        val menuBar = MenuBar()
        menuBar.menus.addAll(

            Menu("File", null,
                MenuItem("Open").apply { setOnAction { EventBus.post(ChooseDirectory()) } },
                MenuItem("New Project").apply { setOnAction { logger.warn("New Project button not implemented yet! ") } },
                MenuItem("Close Project").apply { setOnAction { logger.warn("Close Project button not implemented yet!") } },
                MenuItem("Save All").apply { setOnAction { EventBus.post(SaveAllFiles()) } },
                MenuItem("Exit").apply { setOnAction { modalPane.hide(true) } }
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
                MenuItem("Reload Datapack").apply { setOnAction { EventBus.post(ReloadDatapack()) } },
                MenuItem("Zip Datapack").apply { setOnAction { logger.warn("Zip Datapack button not implemented yet!") } },
                MenuItem("Open Folder").apply { setOnAction { EventBus.post(OpenDatapackFolder()) } }
            ),

            Menu("Help", null,
                MenuItem("Discord").apply { setOnAction { EventBus.post(DiscordLink()) } },
                MenuItem("Github").apply { setOnAction { EventBus.post(GitHubLink()) } },
                MenuItem("Report a Bug").apply { setOnAction { EventBus.post(ReportBugLink()) } }
            )
        )

        dialogContent.top = menuBar
        dialogContent.maxWidth = Region.USE_PREF_SIZE
        dialogContent.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE)
        dialogContent.maxHeight = Region.USE_PREF_SIZE
        return dialogContent
    }
}