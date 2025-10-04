package io.github.frostzie.datapackide.screen.elements.popup.settings

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.settings.SettingsManager
import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.reflect.KClass

/**
 * Main UI for the settings popup window.
 * This class is responsible for the overall layout, while delegating specific UI creation and logic.
 */
class SettingsView(settingsModule: SettingsModule) {
    private val header = SettingsHeader(settingsModule)
    private val nav = SettingsNav(settingsModule)
    private val content = SettingsContent()
    private val footer = SettingsFooter()

    val categoryTreeView get() = nav.categoryTreeView

    fun createContent(): VBox {
        EventBus.register(this)

        val mainContent = SplitPane().apply {
            styleClass.add("main-content")
            items.addAll(nav, content)
            setDividerPositions(0.25)
        }

        return VBox().apply {
            VBox.setVgrow(mainContent, Priority.ALWAYS)
            styleClass.add("settings-window")
            children.addAll(
                header,
                mainContent,
                footer
            )
        }
    }

    fun selectTreeItem(categoryIndex: Int, subCategory: String?) = nav.selectTreeItem(categoryIndex, subCategory)

    fun populateContent(node: Node) = content.populate(node)

    fun showSearchResults(results: List<SettingsManager.SearchResult>) = nav.showSearchResults(results)

    fun createFullCategoryContent(name: String, configClass: KClass<*>) = content.createFullCategoryContent(name, configClass)

    fun createSubCategoryContent(configClass: KClass<*>, subCategoryName: String) = content.createSubCategoryContent(configClass, subCategoryName)
}