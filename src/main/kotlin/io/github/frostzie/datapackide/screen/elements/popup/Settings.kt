package io.github.frostzie.datapackide.screen.elements.popup

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * Settings popup window for configuring DataPack IDE
 */
class Settings(private val parentStage: Stage?) {

    companion object {
        private val logger = LoggerProvider.getLogger("Settings")
    }

    private var stage: Stage? = null

    fun show() {
        createAndShowWindow()
    }

    private fun createAndShowWindow() {
        stage = Stage().apply {
            initStyle(StageStyle.UNDECORATED) // custom undecorated window
            initModality(Modality.APPLICATION_MODAL)
            parentStage?.let { initOwner(it) }
            isResizable = true
            minWidth = 500.0   // Stage sizing must stay in Kotlin
            minHeight = 400.0
        }

        val content = createContent()
        val scene = Scene(content, 600.0, 500.0)

        try {
            val cssUrl = Settings::class.java.getResource("/assets/datapack-ide/themes/Settings.css")
            cssUrl?.let { scene.stylesheets.add(it.toExternalForm()) }
        } catch (e: Exception) {
            logger.warn("Could not load Settings CSS: ${e.message}")
        }

        stage?.scene = scene
        stage?.centerOnScreen()
        stage?.showAndWait()

        logger.info("Settings window closed")
    }

    private fun createContent(): VBox {
        return VBox().apply {
            styleClass.add("settings-window")
            children.addAll(
                createTitleSection(),
                createContentSection(),
                createButtonSection()
            )
        }
    }

    private fun createTitleSection(): VBox {
        return VBox().apply {
            styleClass.add("title-section")

            val titleLabel = Label("Settings").apply {
                styleClass.add("title-label")
            }

            val subtitleLabel = Label("Configure DataPack IDE preferences").apply {
                styleClass.add("subtitle-label")
            }

            children.addAll(titleLabel, subtitleLabel)
        }
    }

    private fun createContentSection(): ScrollPane {
        val contentArea = VBox().apply {
            styleClass.add("content-area")
            children.add(createPlaceholderSection())
        }

        return ScrollPane(contentArea).apply {
            styleClass.add("content-scroll")
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            VBox.setVgrow(this, Priority.ALWAYS)
        }
    }

    private fun createPlaceholderSection(): VBox {
        return VBox().apply {
            styleClass.add("settings-section")

            val sectionTitle = Label("Coming Soon").apply {
                styleClass.add("section-title")
            }

            val placeholder = Label(
                """
                Categories:
                • Main - General settings (Auto startup, language, server support, etc.)
                • UI preferences (font, theme, tab size)
                • Syntax highlighting & editor window options
                • File management (auto-save, backups)
                • Keybinding customization
                """.trimIndent()
            ).apply {
                styleClass.add("placeholder-text")
                isWrapText = true
            }

            children.addAll(sectionTitle, placeholder)
        }
    }

    private fun createButtonSection(): HBox {
        return HBox().apply {
            styleClass.add("button-section")

            val closeButton = Button("Close").apply {
                styleClass.add("close-button")
                setOnAction { handleClose() }
            }

            val resetButton = Button("Reset to Defaults").apply {
                styleClass.add("reset-button")
                isDisable = true
                setOnAction {
                    logger.info("Reset to defaults requested")
                }
            }

            val applyButton = Button("Apply").apply {
                styleClass.add("apply-button")
                isDisable = true
                setOnAction {
                    logger.info("Apply settings requested")
                }
            }

            val spacer = javafx.scene.layout.Region().apply {
                HBox.setHgrow(this, Priority.ALWAYS)
            }

            children.addAll(resetButton, spacer, applyButton, closeButton)
        }
    }

    private fun handleClose() {
        logger.debug("Settings window closing")
        stage?.close()
    }
}
