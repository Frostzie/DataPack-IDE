package io.github.frostzie.datapackide.screen.elements.start

import atlantafx.base.theme.Styles
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MainWindowClose
import io.github.frostzie.datapackide.events.WorkspaceUpdated
import io.github.frostzie.datapackide.loader.fabric.ModVersion
import io.github.frostzie.datapackide.project.Project
import io.github.frostzie.datapackide.project.WorkspaceManager
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ

class StartScreenView : BorderPane() {
    private val logger = LoggerProvider.getLogger("StartScreenView")
    val dragTarget: HBox // Exposed for WindowDrag
    
    private val recentListContent = VBox(5.0)

    init {
        styleClass.add("start-screen")
        
        // Header (Window Controls)
        dragTarget = createHeader()
        top = dragTarget

        // Left Side: Recent Projects
        val recentPane = createRecentProjectsPane()
        left = recentPane
        setMargin(recentPane, Insets(20.0))

        // Center: Actions
        val actionsPane = createActionsPane()
        center = actionsPane
        setMargin(actionsPane, Insets(20.0))
        
        EventBus.register(this)
        refreshRecentProjects()
    }

    private fun createHeader(): HBox {
        val hBox = HBox(10.0)
        hBox.alignment = Pos.CENTER_RIGHT
        hBox.styleClass.add("start-screen-header")
        hBox.padding = Insets(10.0)
        hBox.minHeight = 40.0 // Ensure it has height
        hBox.style = "-fx-background-color: transparent;"

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        val closeBtn = Button()
        closeBtn.graphic = FontIcon(Material2AL.CLOSE)
        closeBtn.styleClass.addAll(Styles.FLAT, Styles.DANGER)
        closeBtn.tooltip = Tooltip("Close")
        closeBtn.setOnAction {
            EventBus.post(MainWindowClose()) 
        }

        hBox.children.addAll(spacer, closeBtn)
        return hBox
    }

    @SubscribeEvent @Suppress("unused")
    fun onWorkspaceUpdated(event: WorkspaceUpdated) {
        Platform.runLater {
            refreshRecentProjects()
        }
    }

    private fun refreshRecentProjects() {
        recentListContent.children.clear()
        val recents = WorkspaceManager.recentProjects
        if (recents.isEmpty()) {
            val emptyLabel = Label("No recent projects")
            emptyLabel.styleClass.add(Styles.TEXT_MUTED)
            recentListContent.children.add(emptyLabel)
        } else {
            recents.forEach { project ->
                recentListContent.children.add(createRecentProjectItem(project))
            }
        }
    }

    private fun createRecentProjectsPane(): VBox {
        val vbox = VBox(10.0)
        vbox.prefWidth = 300.0
        vbox.styleClass.add("recent-projects-pane")
        vbox.padding = Insets(0.0, 20.0, 0.0, 0.0)

        val title = Label("Recent Projects")
        title.styleClass.add(Styles.TITLE_4)
        
        val scrollPane = ScrollPane()
        scrollPane.isFitToWidth = true
        scrollPane.styleClass.add("edge-to-edge")
        scrollPane.content = recentListContent
        VBox.setVgrow(scrollPane, Priority.ALWAYS)

        vbox.children.addAll(title, Separator(), scrollPane)
        return vbox
    }

    private fun createRecentProjectItem(project: Project): Button {
        val btn = Button()
        btn.alignment = Pos.CENTER_LEFT
        btn.maxWidth = Double.MAX_VALUE
        btn.styleClass.add(Styles.FLAT)
        btn.style = "-fx-padding: 8 12 8 12; -fx-alignment: center-left;"
        
        // Icon Logic
        val iconNode = if (project.iconPath != null) {
            try {
                // Use file: URI for local path
                val image = Image(project.iconPath!!.toUri().toString(), 32.0, 32.0, true, true)
                ImageView(image).apply {
                    fitWidth = 32.0
                    fitHeight = 32.0
                }
            } catch (e: Exception) {
                FontIcon(Material2AL.FOLDER).apply { iconSize = 32 }
            }
        } else {
            FontIcon(Material2AL.FOLDER).apply { iconSize = 32 }
        }

        val nameLabel = Label(project.name)
        nameLabel.styleClass.add(Styles.TEXT_BOLD)
        
        val contentBox = VBox(2.0)
        contentBox.children.add(nameLabel)
        
        val metadata = project.metadata
        if (metadata != null) {
            if (metadata.description.isNotBlank()) {
                val descText = metadata.description.take(60).let { if (it.length == 60) "$it..." else it }
                val descLabel = Label(descText)
                descLabel.styleClass.add(Styles.TEXT_SMALL)
                contentBox.children.add(descLabel)
            }
            
            if (!metadata.author.isNullOrBlank()) {
                val authorLabel = Label(metadata.author)
                authorLabel.styleClass.addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED)
                contentBox.children.add(authorLabel)
            }
        } else {
             val pathLabel = Label(project.path.toString())
             pathLabel.styleClass.addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED)
             contentBox.children.add(pathLabel)
        }
        
        btn.graphic = HBox(15.0, iconNode, contentBox).apply { alignment = Pos.CENTER_LEFT }

        btn.setOnAction {
            WorkspaceManager.openSingleProject(project.path)
        }
        return btn
    }

    private fun createActionsPane(): VBox {
        val vBox = VBox(20.0)
        vBox.alignment = Pos.CENTER

        val logo = ImageView(Image("assets/datapack-ide/icon.png"))
        logo.fitHeight = 128.0
        logo.fitWidth = 128.0
        
        val appTitle = Label("DataPack IDE")
        appTitle.styleClass.add(Styles.TITLE_1)

        val versionLabel = Label("Version ${ModVersion.current}")
        versionLabel.styleClass.add(Styles.TEXT_MUTED)

        val actionsBox = VBox(10.0)
        actionsBox.alignment = Pos.CENTER
        actionsBox.maxWidth = 300.0

        val newProjectBtn = createActionButton("New Project", Material2AL.ADD) {
             // TODO: Open New Project
             logger.info("New Project clicked (Not Implemented)")
        }

        val openProjectBtn = createActionButton("Open Project", Material2AL.FOLDER_OPEN) {
             io.github.frostzie.datapackide.utils.file.DirectoryChooseUtils.promptOpenProject(scene.window)
        }

        val openWorldBtn = createActionButton("Open from World", Material2MZ.PUBLIC) {
             logger.info("Open from World clicked (Not Implemented)")
        }

        actionsBox.children.addAll(newProjectBtn, openProjectBtn, openWorldBtn)

        vBox.children.addAll(logo, appTitle, versionLabel, actionsBox)
        return vBox
    }

    private fun createActionButton(text: String, iconCode: Ikon, action: () -> Unit): Button {
        val btn = Button(text)
        btn.graphic = FontIcon(iconCode)
        btn.styleClass.add(Styles.ACCENT)
        btn.prefWidth = 200.0
        btn.prefHeight = 40.0
        btn.setOnAction { action() }
        return btn
    }
}