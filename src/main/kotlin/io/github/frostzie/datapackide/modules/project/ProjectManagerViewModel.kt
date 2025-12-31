package io.github.frostzie.datapackide.modules.project

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.WorkspaceUpdated
import io.github.frostzie.datapackide.project.Project
import io.github.frostzie.datapackide.project.WorkspaceManager
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import java.nio.file.Files

class ProjectManagerViewModel {
    val recentProjects: ObservableList<Project> = FXCollections.observableArrayList()

    init {
        EventBus.register(this)
        refreshRecents()
    }

    @SubscribeEvent
    fun onWorkspaceUpdated(event: WorkspaceUpdated) {
        Platform.runLater {
            refreshRecents()
        }
    }

    fun refreshRecents() {
        recentProjects.setAll(WorkspaceManager.recentProjects)
    }

    fun openProject(project: Project) {
        if (!Files.exists(project.path)) { //TODO: Actually good UI
            val removeBtn = ButtonType("Remove")
            val alert = Alert(
                Alert.AlertType.ERROR,
                "The project directory '${project.name}' does not exist.\nIt may have been moved or deleted.",
                removeBtn, ButtonType.OK
            )
            alert.headerText = "Project Not Found"
            val result = alert.showAndWait()

            if (result.isPresent && result.get() == removeBtn) {
                WorkspaceManager.removeRecentProject(project)
            }
            return
        }

        WorkspaceManager.openSingleProject(project.path)
    }
}