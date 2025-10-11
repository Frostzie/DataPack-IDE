package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.settings.data.ConfigField
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.ui.SettingsControlBuilder
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.HBox.setHgrow
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import io.github.frostzie.datapackide.events.SettingsContentUpdate
import javafx.geometry.Pos
import javafx.scene.control.Label

class SettingsContent : VBox() {

    init {
        styleClass.add("settings-content-area")
        setVgrow(this, Priority.ALWAYS)
        setHgrow(this, Priority.ALWAYS)
        EventBus.register(this)
    }

    @SubscribeEvent
    fun onContentUpdate(event: SettingsContentUpdate) {
        if (event.sections.isEmpty()) {
            val noResultsLabel = Label("No results found.").apply {
                styleClass.add("no-results-label")
            }
            val container = VBox(noResultsLabel).apply {
                styleClass.add("category-content")
                alignment = Pos.CENTER
            }
            children.setAll(container)
            return
        }

        val content = VBox().apply {
            styleClass.add("category-content")
            spacing = 20.0

            val categoryTitle = Label(event.title).apply {
                styleClass.add("category-title")
            }
            children.add(categoryTitle)

            event.sections.forEach { sectionData ->
                val section = createSubCategorySection(sectionData.name, sectionData.description, sectionData.fields, event.filterFields)
                if (section.children.any { it.isVisible }) {
                    children.add(section)
                }
            }
        }

        val scrollPane = ScrollPane(content).apply {
            styleClass.add("category-scroll")
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }
        children.setAll(scrollPane)
    }


    private fun createSubCategorySection(subCategoryName: String, description: String?, fields: List<ConfigField>, filterFields: Set<ConfigField>?): VBox {
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
                    setHgrow(this, Priority.ALWAYS)
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

            val fieldsToShow = if (filterFields != null) fields.filter { it in filterFields } else fields

            fieldsToShow.forEach { field ->
                children.add(createFieldControl(field))
            }

            val hasVisibleFields = fieldsToShow.isNotEmpty()
            this.isVisible = hasVisibleFields
            this.isManaged = hasVisibleFields
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
        }
    }
}