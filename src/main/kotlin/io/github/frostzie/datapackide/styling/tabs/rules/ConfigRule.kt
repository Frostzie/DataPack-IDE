package io.github.frostzie.datapackide.styling.tabs.rules

import io.github.frostzie.datapackide.config.ConfigManager
import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.styling.common.StylePriority
import io.github.frostzie.datapackide.styling.tabs.TabStyle
import io.github.frostzie.datapackide.styling.tabs.TabStyleRule

/**
 * A style rule that applies a distinct visual style to tabs representing
 * files inside the Datapack-ide config folder
 */
class ConfigRule : TabStyleRule {
    override val priority: Int = StylePriority.SPECIFIC_RULES

    private val configDirPath = ConfigManager.configDir.toAbsolutePath().toString()

    override fun appliesTo(context: TextEditorViewModel.TabData): Boolean {
        val filePath = context.filePath.toAbsolutePath().toString()
        return filePath.startsWith(configDirPath)
    }

    override fun getStyle(context: TextEditorViewModel.TabData): TabStyle {
        // Using a hardcoded color for now. Might allow changing in configs later on.
        return TabStyle(textColor = "#ffffff", isItalic = true, backgroundColor = "#755709")
    }
}