package io.github.frostzie.datapackide.screen.elements.popup

import io.github.frostzie.datapackide.utils.LoggerProvider
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.UIActionEvent
import io.github.frostzie.datapackide.events.UIAction
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * Settings popup window
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
            initStyle(StageStyle.UNDECORATED)
            initModality(Modality.APPLICATION_MODAL)
            parentStage?.let { initOwner(it) }
            isResizable = true
            minWidth = 500.0
            minHeight = 400.0
        }

        val content = createContent()
        val scene = Scene(content, 600.0, 500.0)

        try {
            CSSManager.applyPopupStyles(scene, "Settings.css")
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

    //TODO: both handleReloadAssets and handleResetToDefaults should be moved to Advanced Settings section when settings reworked!
    private fun handleReloadAssets() {
        logger.info("Reloading assets and styles...")
        try {
            EventBus.post(UIActionEvent(UIAction.RELOAD_STYLES))
            logger.info("Assets and styles reload event posted")
        } catch (e: Exception) {
            logger.error("Failed to post reload assets event", e)
        }
    }

    private fun handleResetToDefaults() {
        logger.info("Reset to defaults requested. This will overwrite custom styles.")
        try {
            EventBus.post(UIActionEvent(UIAction.RESET_STYLES_TO_DEFAULT))
            logger.info("Reset to defaults event posted")
        } catch (e: Exception) {
            logger.error("Failed to post reset to defaults event", e)
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

            val reloadButton = Button("Reload Assets & Styles").apply {
                setOnAction {
                    handleReloadAssets()
                }
            }

            children.addAll(sectionTitle, placeholder, reloadButton)
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
                setOnAction {
                    handleResetToDefaults()
                }
            }

            val applyButton = Button("Apply").apply {
                styleClass.add("apply-button")
                isDisable = true
                setOnAction {
                    logger.info("Apply settings requested")
                }
            }

            val spacer = Region().apply {
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