package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.HighlightField
import io.github.frostzie.datapackide.events.SettingsContentUpdate
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.ui.SettingsControlBuilder
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.HBox.setHgrow
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Duration

class SettingsContent : VBox() {
    private val fieldControlMap = mutableMapOf<ConfigField, VBox>()
    private lateinit var scrollPane: ScrollPane

    init {
        styleClass.add("settings-content-area")
        setVgrow(this, Priority.ALWAYS)
        setHgrow(this, Priority.ALWAYS)
        EventBus.register(this)
    }

    @SubscribeEvent
    fun onContentUpdate(event: SettingsContentUpdate) {
        fieldControlMap.clear()
        val content = VBox().apply {
            styleClass.add("category-content")
            spacing = 20.0

            val categoryTitle = Label(event.title).apply {
                styleClass.add("category-title")
            }
            children.add(categoryTitle)

            event.sections.forEach { sectionData ->
                children.add(createSubCategorySection(sectionData.name, sectionData.description, sectionData.fields))
            }
        }

        scrollPane = ScrollPane(content).apply {
            styleClass.add("category-scroll")
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }
        children.setAll(scrollPane)
    }

    @SubscribeEvent
    fun onHighlightField(event: HighlightField) {
        val controlToHighlight = fieldControlMap[event.field]
        controlToHighlight?.let { control ->
            Platform.runLater {
                // Scroll the item into view
                val viewportHeight = scrollPane.viewportBounds.height
                val contentHeight = scrollPane.content.boundsInLocal.height
                if (contentHeight > viewportHeight) {
                    val controlBounds = control.boundsInParent
                    val controlY = controlBounds.minY + controlBounds.height / 2.0
                    val vValue = (controlY - viewportHeight / 2.0) / (contentHeight - viewportHeight)
                    scrollPane.vvalue = vValue.coerceIn(0.0, 1.0)
                }

                // Apply highlight
                control.requestFocus()
                control.styleClass.add("highlighted")
                val timeline = Timeline(KeyFrame(Duration.seconds(2.0), {
                    control.styleClass.remove("highlighted")
                }))
                timeline.play()
            }
        }
    }

    private fun createSubCategorySection(subCategoryName: String, description: String?, fields: List<ConfigField>): VBox {
        return VBox().apply {
            styleClass.add("subcategory-section")
            spacing = 10.0

            val subCategoryHeader = HBox().apply {
                styleClass.add("subcategory-header")

                val subTitle = Label(subCategoryName).apply {
                    styleClass.add("subcategory-title")
                }

                val separator = Separator().apply {
                    styleClass.add("subcategory-separator")
                    HBox.setHgrow(this, Priority.ALWAYS)
                }

                children.addAll(subTitle, separator)
            }

            children.add(subCategoryHeader)

            if (!description.isNullOrBlank()) {
                val descLabel = Label(description).apply {
                    styleClass.add("subcategory-description")
                    isWrapText = true
                }
                children.add(descLabel)
            }

            fields.forEach { field ->
                children.add(createFieldControl(field))
            }
        }
    }

    private fun createFieldControl(field: ConfigField): VBox {
        return VBox().apply {
            styleClass.add("field-control")
            spacing = 5.0

            val nameLabel = Label(field.name).apply {
                styleClass.add("field-name")
            }
            children.add(nameLabel)

            if (field.description.isNotEmpty()) {
                val descLabel = Label(field.description).apply {
                    styleClass.add("field-description")
                    isWrapText = true
                }
                children.add(descLabel)
            }

            children.add(SettingsControlBuilder.createControl(field))
            fieldControlMap[field] = this
        }
    }
}