package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.*
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.settings.data.CategoryItem
import io.github.frostzie.datapackide.settings.data.CategoryType
import io.github.frostzie.datapackide.settings.data.SearchResult
import io.github.frostzie.datapackide.utils.UIConstants
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

/**
 * Settings navigation panel - displays category tree and search results.
 */
class SettingsNav : VBox() {
    private lateinit var searchField: TextField
    private lateinit var searchResults: ListView<SearchResult>
    private lateinit var categoryTreeView: TreeView<CategoryItem>

    init {
        styleClass.add("left-panel")
        minWidth = UIConstants.SETTINGS_SIDE_PANEL_MIN_WIDTH
        maxWidth = UIConstants.SETTINGS_SIDE_PANEL_MAX_WIDTH

        createViewComponents()

        val viewStack = StackPane().apply {
            children.addAll(categoryTreeView, searchResults)
            setVgrow(this, Priority.ALWAYS)
        }

        children.addAll(
            createSearchSection(),
            viewStack
        )

        EventBus.register(this)
    }

    private fun createSearchSection(): HBox {
        searchField = TextField().apply {
            styleClass.add("search-field")
            promptText = "Search"
            textProperty().addListener(ChangeListener { _, _, newValue ->
                EventBus.post(SettingsSearchQueryChanged(newValue))
            })
        }

        return HBox().apply {
            styleClass.add("search-section")
            children.add(searchField)
            HBox.setHgrow(searchField, Priority.ALWAYS)
        }
    }

    private fun createViewComponents() {
        categoryTreeView = TreeView<CategoryItem>().apply {
            styleClass.add("category-tree")
            isShowRoot = false
            selectionModel.selectedItemProperty().addListener { _, _, newItem ->
                newItem?.value?.let { EventBus.post(SettingsCategorySelected(it)) }
            }

            setCellFactory {
                object : TreeCell<CategoryItem>() {
                    init { isWrapText = true }
                    override fun updateItem(item: CategoryItem?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty) null else item?.name
                    }
                }
            }
        }

        searchResults = ListView<SearchResult>().apply {
            styleClass.add("search-results")
            isVisible = false
            isManaged = false

            setOnMouseClicked {
                selectionModel.selectedItem?.let {
                    EventBus.post(SettingsSearchResultSelected(it))
                }
            }

            setCellFactory {
                object : ListCell<SearchResult>() {
                    private val titleLabel = Label().apply {
                        styleClass.add("search-result-title")
                        isWrapText = true
                    }
                    private val pathLabel = Label().apply {
                        styleClass.add("search-result-path")
                        isWrapText = true
                    }
                    private val descriptionLabel = Label().apply {
                        styleClass.add("search-result-description")
                        isWrapText = true
                    }
                    private val content = VBox(titleLabel, pathLabel, descriptionLabel).apply {
                        styleClass.add("search-result-item")
                        prefWidth = 0.0
                    }

                    override fun updateItem(item: SearchResult?, empty: Boolean) {
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

    /**
     * Receives available categories from the module via event bus.
     * Builds the category tree structure for display.
     */
    @SubscribeEvent
    fun onCategoriesAvailable(event: SettingsCategoriesAvailable) {
        val rootItem = TreeItem(CategoryItem("Settings", CategoryType.ROOT))
        rootItem.isExpanded = true

        event.categories.forEach { categoryData ->
            val categoryItem = TreeItem(
                CategoryItem(
                    categoryData.name,
                    CategoryType.MAIN_CATEGORY,
                    categoryData.configClass
                )
            )
            categoryItem.isExpanded = true

            categoryData.subCategories.forEach { subCategoryName ->
                val subItem = TreeItem(
                    CategoryItem(
                        subCategoryName,
                        CategoryType.SUB_CATEGORY,
                        categoryData.configClass,
                        subCategoryName
                    )
                )
                categoryItem.children.add(subItem)
            }
            rootItem.children.add(categoryItem)
        }

        categoryTreeView.root = rootItem

        Platform.runLater {
            categoryTreeView.selectionModel.select(1)
        }
    }

    /**
     * Selects a tree item based on indices.
     * Used when navigating from search results.
     */
    @SubscribeEvent
    fun onSelectTreeItem(event: SelectTreeItem) {
        Platform.runLater {
            val rootItem = categoryTreeView.root
            if (event.categoryIndex < 0 || event.categoryIndex >= rootItem.children.size) return@runLater

            val mainCategoryItem = rootItem.children[event.categoryIndex]

            val targetItem = if (event.subCategory != null && event.subCategory != "General") {
                mainCategoryItem.children.find { it.value.subCategory == event.subCategory }
            } else {
                mainCategoryItem
            }

            targetItem?.let {
                categoryTreeView.selectionModel.select(it)

                // Expand parent items
                var parent = it.parent
                while (parent != null && !parent.isExpanded) {
                    parent.isExpanded = true
                    parent = parent.parent
                }
            }
        }
    }

    /**
     * Receives search results from the module and displays them.
     * Toggles between tree view and search results view.
     */
    @SubscribeEvent
    fun onSearchResultsAvailable(event: SettingsSearchResultsAvailable) {
        val showResults = event.results.isNotEmpty()

        searchResults.items.setAll(event.results)
        searchResults.isVisible = showResults
        searchResults.isManaged = showResults

        categoryTreeView.isVisible = !showResults
        categoryTreeView.isManaged = !showResults
    }
}