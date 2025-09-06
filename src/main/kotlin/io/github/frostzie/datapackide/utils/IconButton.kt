package io.github.frostzie.datapackide.utils

import javafx.css.CssMetaData
import javafx.css.ParsedValue
import javafx.css.Styleable
import javafx.css.StyleableProperty
import javafx.css.StyleableStringProperty
import javafx.css.StyleConverter
import javafx.scene.control.Button
import javafx.scene.text.Font
import java.util.Collections

/**
 * A custom Button that allows its text content (the icon character) to be set via a CSS property.
 * The custom CSS property is {@code -fx-icon-character}.
 */
class IconButton(initializer: IconButton.() -> Unit = {}) : Button() {

    init {
        initializer()
    }

    /**
     * The icon character to be displayed on the button. This is settable from CSS.
     */
    private val iconCharacter = object : StyleableStringProperty() {
        override fun invalidated() {
            text = value
        }

        override fun getBean(): Any {
            return this@IconButton
        }

        override fun getName(): String {
            return "iconCharacter"
        }

        override fun getCssMetaData(): CssMetaData<IconButton, String> {
            return StyleableProperties.ICON_CHARACTER
        }
    }

    private object StyleableProperties {
        private val UNICODE_CONVERTER = object : StyleConverter<String, String>() {
            override fun convert(value: ParsedValue<String, String>, font: Font): String {
                val rawValue = value.value
                if (rawValue.startsWith("\\") && rawValue.length > 1) {
                    return try {
                        val hexCode = rawValue.substring(1)
                        val codePoint = Integer.parseInt(hexCode, 16)
                        String(Character.toChars(codePoint))
                    } catch (e: NumberFormatException) {
                        rawValue
                    }
                }
                return rawValue
            }
        }

        val ICON_CHARACTER: CssMetaData<IconButton, String> =
            object : CssMetaData<IconButton, String>("-fx-icon-character", UNICODE_CONVERTER) {
                override fun isSettable(node: IconButton): Boolean {
                    return !node.iconCharacter.isBound
                }

                override fun getStyleableProperty(node: IconButton): StyleableProperty<String> {
                    return node.iconCharacter
                }
            }

        val STYLEABLES: List<CssMetaData<out Styleable, *>>
        init {
            val styleables = ArrayList<CssMetaData<out Styleable, *>>(getClassCssMetaData())
            styleables.add(ICON_CHARACTER)
            STYLEABLES = Collections.unmodifiableList(styleables)
        }
    }

    override fun getControlCssMetaData(): List<CssMetaData<out Styleable, *>> {
        return StyleableProperties.STYLEABLES
    }
}