package io.github.frostzie.datapackide.utils

import atlantafx.base.theme.*
import javafx.application.Application

object ThemeUtils {

    fun applyTheme(themeName: String) {
        val theme = when (themeName) {
            "Primer Light" -> PrimerLight()
            "Primer Dark" -> PrimerDark()
            "Nord Light" -> NordLight()
            "Nord Dark" -> NordDark()
            "Cupertino Light" -> CupertinoLight()
            "Cupertino Dark" -> CupertinoDark()
            "Dracula" -> Dracula()
            else -> PrimerDark() // Default theme
        }
        Application.setUserAgentStylesheet(theme.userAgentStylesheet)
    }
}