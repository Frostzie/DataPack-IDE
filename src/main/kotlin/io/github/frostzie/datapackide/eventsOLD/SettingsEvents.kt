package io.github.frostzie.datapackide.eventsOLD

import io.github.frostzie.datapackide.screen.elements.popup.SettingsView
import io.github.frostzie.datapackide.settings.SettingsManager
import javafx.scene.Node

@Deprecated("Replacing with newer system")
class SettingsSaveRequestEvent

@Deprecated("Replacing with newer system")
class SettingsCloseRequestEvent

@Deprecated("Replacing with newer system")
data class SettingsCategorySelectedEvent(val categoryItem: SettingsView.CategoryItem)

@Deprecated("Replacing with newer system")
data class SettingsSearchEvent(val query: String)

@Deprecated("Replacing with newer system")
data class PopulateSettingsContentEvent(val content: Node)

@Deprecated("Replacing with newer system")
data class ShowSearchResultsEvent(val results: List<SettingsManager.SearchResult>)

@Deprecated("Replacing with newer system")
data class SelectTreeItemEvent(val categoryIndex: Int, val subCategory: String?)

@Deprecated("Replacing with newer system")
data class SettingsSearchResultSelectedEvent(val result: SettingsManager.SearchResult)