package io.github.frostzie.datapackide.eventsOLD

import io.github.frostzie.datapackide.screen.elements.popup.SettingsView
import io.github.frostzie.datapackide.settings.SettingsManager
import javafx.scene.Node

@Deprecated("Replacing with newer system")

class SettingsSaveRequestEvent


class SettingsCloseRequestEvent


data class SettingsCategorySelectedEvent(val categoryItem: SettingsView.CategoryItem)


data class SettingsSearchEvent(val query: String)


data class PopulateSettingsContentEvent(val content: Node)


data class ShowSearchResultsEvent(val results: List<SettingsManager.SearchResult>)


data class SelectTreeItemEvent(val categoryIndex: Int, val subCategory: String?)

data class SettingsSearchResultSelectedEvent(val result: SettingsManager.SearchResult)