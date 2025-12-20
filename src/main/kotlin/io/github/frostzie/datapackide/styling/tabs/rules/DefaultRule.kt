package io.github.frostzie.datapackide.styling.tabs.rules

import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.styling.common.StylePriority
import io.github.frostzie.datapackide.styling.tabs.TabStyle
import io.github.frostzie.datapackide.styling.tabs.TabStyleRule

class DefaultRule : TabStyleRule {

    override val priority: Int = StylePriority.BASE

    /**
     * This rule applies to every tab, unconditionally.
     */
    override fun appliesTo(context: TextEditorViewModel.TabData): Boolean {
        return true
    }

    override fun getStyle(context: TextEditorViewModel.TabData): TabStyle {
        return TabStyle(
            textColor = "-color-fg-default",
            isBold = false,
            isItalic = false,
            isUnderline = false
        )
    }
}
