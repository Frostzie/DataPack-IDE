package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.utils.ui.SettingsControlBuilder
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.HBox.setHgrow
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.reflect.KClass

class SettingsContent : VBox() {
    init {
        styleClass.add("settings-content-area")
        setVgrow(this, Priority.ALWAYS)
        setHgrow(this, Priority.ALWAYS)
    }

    fun populate(content: Node) {
        children.setAll(content)
    }

    fun createFullCategoryContent(categoryName: String, configClass: KClass<*>): ScrollPane {
        val content = VBox().apply {
            styleClass.add("category-content")
            spacing = 20.0

            val categoryTitle = Label(categoryName.replaceFirstChar { it.uppercase() } + " Settings").apply {
                styleClass.add("category-title")
            }
            children.add(categoryTitle)

            val nestedCategories = SettingsManager.getNestedCategories(configClass)
            nestedCategories.forEach { (subCategoryName, fields) ->
                children.add(createSubCategorySection(subCategoryName, fields))
            }
        }

        return ScrollPane(content).apply {
            styleClass.add("category-scroll")
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }
    }

    fun createSubCategoryContent(configClass: KClass<*>, subCategoryName: String): ScrollPane {
        val nestedCategories = SettingsManager.getNestedCategories(configClass)
        val fields = nestedCategories[subCategoryName] ?: emptyList()

        val content = VBox().apply {
            styleClass.add("category-content")
            spacing = 15.0

            val title = Label("$subCategoryName Settings").apply {
                styleClass.add("category-title")
            }
            children.add(title)

            fields.forEach { field ->
                children.add(createFieldControl(field))
            }
        }

        return ScrollPane(content).apply {
            styleClass.add("category-scroll")
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }
    }

    private fun createSubCategorySection(subCategoryName: String, fields: List<SettingsManager.ConfigField>): VBox {
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

            val categoryDesc = fields.firstOrNull()?.category?.desc
            if (!categoryDesc.isNullOrBlank()) {
                val descLabel = Label(categoryDesc).apply {
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

    private fun createFieldControl(field: SettingsManager.ConfigField): VBox {
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