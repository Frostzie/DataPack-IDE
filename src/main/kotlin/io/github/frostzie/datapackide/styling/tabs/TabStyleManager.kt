package io.github.frostzie.datapackide.styling.tabs

import io.github.frostzie.datapackide.modules.main.TextEditorViewModel
import io.github.frostzie.datapackide.styling.common.BaseStyleManager

/**
 * Style manager for text editor tabs.
 *
 * This object extends the [BaseStyleManager], providing the specific types and the merge logic
 * required for styling tabs.
 */
object TabStyleManager : BaseStyleManager<TextEditorViewModel.TabData, TabStyle>(
    emptyStyle = TabStyle(),
    mergeStyles = { current, new ->
        if (current.isFinal) {
            current
        } else {
            current.copy(
                textColor = new.textColor ?: current.textColor,
                backgroundColor = new.backgroundColor ?: current.backgroundColor,
                isBold = new.isBold ?: current.isBold,
                isItalic = new.isItalic ?: current.isItalic,
                isUnderline = new.isUnderline ?: current.isUnderline,
                iconSource = new.iconSource ?: current.iconSource,
                prefix = new.prefix ?: current.prefix,
                // If either style is final, the resulting merged style is final.
                isFinal = current.isFinal || new.isFinal
            )
        }
    }
)