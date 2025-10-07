package io.github.frostzie.datapackide.events

import io.github.frostzie.datapackide.modules.popup.settings.SettingsModule
import io.github.frostzie.datapackide.screen.elements.popup.settings.SettingsNav
import io.github.frostzie.datapackide.settings.ConfigField
import kotlin.reflect.KClass

class SettingsWindowOpen
class SettingsWindowCloseSave


/**
 *  Settings events
 */
class SettingsSave
data class SettingsSearchQueryChanged(val query: String)
data class SettingsCategorySelected(val item: SettingsNav.CategoryItem)
data class SettingsSearchResultSelected(val result: SettingsModule.SearchResult)
data class SettingsWindowDragged(val screenX: Double, val screenY: Double)
data class SettingsWindowDragStarted(val sceneX: Double, val sceneY: Double)

class RequestSettingsCategories
data class CategoryData(val name: String, val configClass: KClass<*>, val subCategories: List<String>)
data class SettingsCategoriesAvailable(val categories: List<CategoryData>)
data class SectionData(val name: String, val description: String?, val fields: List<ConfigField>)
data class SettingsContentUpdate(val title: String, val sections: List<SectionData>)
data class SettingsSearchResultsAvailable(val results: List<SettingsModule.SearchResult>)
data class SelectTreeItem(val categoryIndex: Int, val subCategory: String?)