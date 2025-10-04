package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlin.reflect.KClass

class SettingsNav(private val settingsModule: SettingsModule) : VBox() {
    lateinit var searchField: TextField
    lateinit var categoryTreeView: TreeView<CategoryItem>
    lateinit var searchResults: ListView<SettingsManager.SearchResult>

    init {
        styleClass.add("left-panel")
        minWidth = UIConstants.SETTINGS_SIDE_PANEL_MIN_WIDTH
        maxWidth = UIConstants.SETTINGS_SIDE_PANEL_MAX_WIDTH

        createContentAndSearchAreas()
        createCategoryTreeView()

        val viewStack = StackPane().apply {
            children.addAll(categoryTreeView, searchResults)
            setVgrow(this, Priority.ALWAYS)
        }

        children.addAll(
            createSearchSection(),
            viewStack
        )
    }

    private fun createSearchSection(): HBox {
        searchField = TextField().apply {
            styleClass.add("search-field")
            promptText = "Search"
            textProperty().addListener(ChangeListener { _, _, newValue ->
                settingsModule.handleSearch(newValue)
            })
        }

        return HBox().apply {
            styleClass.add("search-section")
            children.add(searchField)
            HBox.setHgrow(searchField, Priority.ALWAYS)
        }
    }

    private fun createCategoryTreeView() {
        val categories = SettingsManager.getConfigCategories()

        categoryTreeView = TreeView<CategoryItem>().apply {
            styleClass.add("category-tree")

            val rootItem = TreeItem(CategoryItem("Settings", CategoryType.ROOT))
            rootItem.isExpanded = true

            categories.forEach { (categoryName, configClass) ->
                val categoryItem = TreeItem(
                    CategoryItem(
                        categoryName.replaceFirstChar { it.uppercase() },
                        CategoryType.MAIN_CATEGORY,
                        configClass
                    )
                )
                categoryItem.isExpanded = true

                val nestedCategories = SettingsManager.getNestedCategories(configClass)
                nestedCategories.keys.forEach { subCategoryName ->
                    val subItem =
                        TreeItem(CategoryItem(subCategoryName, CategoryType.SUB_CATEGORY, configClass, subCategoryName))
                    categoryItem.children.add(subItem)
                }

                rootItem.children.add(categoryItem)
            }

            root = rootItem
            isShowRoot = false

            selectionModel.selectedItemProperty().addListener { _, _, newItem ->
                newItem?.value?.let { settingsModule.handleCategorySelection(it) }
            }

            setCellFactory {
                object : TreeCell<CategoryItem>() {
                    init {
                        isWrapText = true
                    }

                    override fun updateItem(item: CategoryItem?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty) null else item?.name
                    }
                }
            }
        }
    }

    private fun createContentAndSearchAreas() {
        searchResults = ListView<SettingsManager.SearchResult>().apply {
            styleClass.add("search-results")
            isVisible = false
            isManaged = false

            setOnMouseClicked { event ->
                selectionModel.selectedItem?.let { settingsModule.handleSearchResultSelection(it) }
            }

            setCellFactory {
                object : ListCell<SettingsManager.SearchResult>() {
                    private val titleLabel = Label().apply { styleClass.add("search-result-title"); isWrapText = true }
                    private val pathLabel = Label().apply { styleClass.add("search-result-path"); isWrapText = true }
                    private val descriptionLabel = Label().apply { styleClass.add("search-result-description"); isWrapText = true }
                    private val content = VBox(titleLabel, pathLabel, descriptionLabel).apply {
                        styleClass.add("search-result-item")
                        prefWidth = 0.0
                    }

                    override fun updateItem(item: SettingsManager.SearchResult?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty || item == null) {
                            graphic = null
                        } else {
                            titleLabel.text = item.field.name
                            pathLabel.text = "${item.mainCategory.replaceFirstChar { it.uppercase() }} > ${item.subCategory}"
                            descriptionLabel.text = item.field.description
                            graphic = content
                        }
                    }
                }
            }
        }
    }

    fun selectTreeItem(categoryIndex: Int, subCategory: String?) {
        Platform.runLater {
            val rootItem = categoryTreeView.root
            if (categoryIndex < 0 || categoryIndex >= rootItem.children.size) return@runLater

            val mainCategoryItem = rootItem.children[categoryIndex]

            val targetItem = if (subCategory != null && subCategory != "General") {
                mainCategoryItem.children.find { it.value.subCategory == subCategory }
            } else {
                mainCategoryItem
            }

            targetItem?.let {
                categoryTreeView.selectionModel.select(it)
                if (!mainCategoryItem.isExpanded) {
                    mainCategoryItem.isExpanded = true
                }
            }
        }
    }

    fun showSearchResults(results: List<SettingsManager.SearchResult>) {
        val showResults = results.isNotEmpty()
        searchResults.items.setAll(results)
        searchResults.isVisible = showResults
        searchResults.isManaged = showResults
        categoryTreeView.isVisible = !showResults
        categoryTreeView.isManaged = !showResults
    }

    data class CategoryItem(
        val name: String,
        val type: CategoryType,
        val configClass: KClass<*>? = null,
        val subCategory: String? = null
    ) {
        override fun toString(): String = name
    }

    enum class CategoryType {
        ROOT, MAIN_CATEGORY, SUB_CATEGORY
    }
}