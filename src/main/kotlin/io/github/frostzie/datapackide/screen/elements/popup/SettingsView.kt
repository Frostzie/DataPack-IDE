package io.github.frostzie.datapackide.screen.elements.popup

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.eventsOLD.PopulateSettingsContentEvent
import io.github.frostzie.datapackide.eventsOLD.SelectTreeItemEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsCategorySelectedEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsCloseRequestEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsSaveRequestEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsSearchEvent
import io.github.frostzie.datapackide.eventsOLD.SettingsSearchResultSelectedEvent
import io.github.frostzie.datapackide.eventsOLD.ShowSearchResultsEvent
import io.github.frostzie.datapackide.modules.popup.SettingsModule
import io.github.frostzie.datapackide.settings.SettingsManager
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent
import io.github.frostzie.datapackide.utils.UIConstants
import io.github.frostzie.datapackide.utils.ui.SettingsControlBuilder
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.*
import javafx.scene.layout.*
import kotlin.reflect.KClass

/**
 * Builds the UI for the settings popup window.
 * This class is responsible for layout and component creation, while the logic is handled by SettingsController.
 */
class SettingsView(val settingsModule: SettingsModule) {

    lateinit var searchField: TextField
    lateinit var categoryTreeView: TreeView<CategoryItem>
    lateinit var contentArea: VBox
    lateinit var searchResults: ListView<SettingsManager.SearchResult>

    fun createContent(): VBox {
        EventBus.register(this)
        return VBox().apply {
            val mainContent = createMainContent()
            VBox.setVgrow(mainContent, Priority.ALWAYS)

            styleClass.add("settings-window")
            children.addAll(
                createTitleSection(),
                mainContent,
                createButtonSection()
            )
        }
    }

    private fun createTitleSection(): HBox {
        return HBox().apply {
            styleClass.add("title-section")

            val titleLabel = Label("Settings").apply {
                styleClass.add("title-label")
            }

            val spacer = Region().apply {
                HBox.setHgrow(this, Priority.ALWAYS)
            }

            val closeButton = Button("✕").apply {
                styleClass.add("title-close-button")
                setOnAction { EventBus.post(SettingsCloseRequestEvent()) }
            }

            children.addAll(titleLabel, spacer, closeButton)

            setOnMousePressed { event ->
                settingsModule.xOffset = event.sceneX
                settingsModule.yOffset = event.sceneY
            }
            setOnMouseDragged { event ->
                settingsModule.stage?.x = event.screenX - settingsModule.xOffset
                settingsModule.stage?.y = event.screenY - settingsModule.yOffset
            }
        }
    }

    private fun createSearchSection(): HBox {
        searchField = TextField().apply {
            styleClass.add("search-field")
            promptText = "Search"
            textProperty().addListener(ChangeListener { _, _, newValue ->
                EventBus.post(SettingsSearchEvent(newValue))
            })
        }

        return HBox().apply {
            styleClass.add("search-section")
            children.add(searchField)
            HBox.setHgrow(searchField, Priority.ALWAYS)
        }
    }

    private fun createMainContent(): SplitPane {
        createContentAndSearchAreas()
        createCategoryTreeView()

        val viewStack = StackPane().apply {
            children.addAll(categoryTreeView, searchResults)
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        val leftPanel = VBox().apply {
            styleClass.add("left-panel")
            minWidth = UIConstants.SETTINGS_SIDE_PANEL_MIN_WIDTH
            maxWidth = UIConstants.SETTINGS_SIDE_PANEL_MAX_WIDTH
            children.addAll(
                createSearchSection(),
                viewStack
            )
        }

        return SplitPane().apply {
            styleClass.add("main-content")
            items.addAll(leftPanel, contentArea)
            setDividerPositions(0.25)
        }
    }

    private fun createCategoryTreeView() {
        val categories = SettingsManager.getConfigCategories()

        categoryTreeView = TreeView<CategoryItem>().apply {
            styleClass.add("category-tree")

            val rootItem = TreeItem(CategoryItem("Settings", CategoryType.ROOT))
            rootItem.isExpanded = true

            categories.forEach { (categoryName, configClass) ->
                val categoryItem = TreeItem(CategoryItem(categoryName.replaceFirstChar { it.uppercase() }, CategoryType.MAIN_CATEGORY, configClass))
                categoryItem.isExpanded = true

                val nestedCategories = SettingsManager.getNestedCategories(configClass)
                nestedCategories.keys.forEach { subCategoryName ->
                    val subItem = TreeItem(CategoryItem(subCategoryName, CategoryType.SUB_CATEGORY, configClass, subCategoryName))
                    categoryItem.children.add(subItem)
                }

                rootItem.children.add(categoryItem)
            }

            root = rootItem
            isShowRoot = false

            selectionModel.selectedItemProperty().addListener { _, _, newItem ->
                newItem?.let { EventBus.post(SettingsCategorySelectedEvent(it.value)) }
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
            setOnMouseClicked { event ->
            }
        }
    }

    private fun createContentAndSearchAreas() {
        contentArea = VBox().apply {
            styleClass.add("settings-content-area")
            VBox.setVgrow(this, Priority.ALWAYS)
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        // Create search results view (initially hidden)
        searchResults = ListView<SettingsManager.SearchResult>().apply {
            styleClass.add("search-results")
            isVisible = false
            isManaged = false

            setOnMouseClicked { event ->
                selectionModel.selectedItem?.let { EventBus.post(SettingsSearchResultSelectedEvent(it)) }
            }

            setCellFactory {
                object : ListCell<SettingsManager.SearchResult>() {
                    // Create nodes once for efficiency, not in every updateItem call.
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

    fun createFullCategoryContent(categoryName: String, configClass: KClass<*>): ScrollPane {
        val content = VBox().apply {
            styleClass.add("category-content")
            spacing = 20.0

            val categoryTitle = Label(categoryName.replaceFirstChar { it.uppercase() } + " Settings").apply {
                styleClass.add("category-title")
            }
            children.add(categoryTitle)

            // Group fields by subcategory
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
                    HBox.setHgrow(this, Priority.ALWAYS)
                }

                children.addAll(subTitle, separator)
            }

            children.add(subCategoryHeader)

            // Add description if available
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

    private fun createButtonSection(): HBox {
        return HBox().apply {
            styleClass.add("button-section")

            val spacer = Region().apply {
                HBox.setHgrow(this, Priority.ALWAYS)
            }

            val applyButton = Button("Apply").apply {
                styleClass.add("apply-button")
                setOnAction { EventBus.post(SettingsSaveRequestEvent()) }
            }

            val closeButton = Button("Close").apply {
                styleClass.add("close-button")
                setOnAction { EventBus.post(SettingsCloseRequestEvent()) }
            }

            children.addAll(spacer, applyButton, closeButton)
        }
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

    @SubscribeEvent
    fun onPopulateContent(event: PopulateSettingsContentEvent) {
        contentArea.children.setAll(event.content)
    }

    @SubscribeEvent
    fun onShowSearchResults(event: ShowSearchResultsEvent) {
        val showResults = event.results.isNotEmpty()
        searchResults.items.setAll(event.results)
        searchResults.isVisible = showResults
        searchResults.isManaged = showResults
        categoryTreeView.isVisible = !showResults
        categoryTreeView.isManaged = !showResults
    }

    @SubscribeEvent
    fun onSelectTreeItem(event: SelectTreeItemEvent) {
        selectTreeItem(event.categoryIndex, event.subCategory)
    }
}