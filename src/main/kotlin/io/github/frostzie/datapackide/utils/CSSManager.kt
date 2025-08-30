package io.github.frostzie.datapackide.utils

import javafx.scene.Scene
import net.fabricmc.loader.api.FabricLoader
import java.net.URL

/**
 * Handles loading and applying stylesheets to scenes and components
 */
object CSSManager {
    private val logger = LoggerProvider.getLogger("CSSManager")

    private const val CSS_BASE_PATH = "/assets/datapack-ide/themes/"
    private val CUSTOM_CSS_PATH = FabricLoader.getInstance().configDir.toString() + "/datapack-ide/themes/"

    private val searchPaths = listOf(
        "styles/top-bar/",
        "styles/",
        ""
    )

    private val cssFiles = listOf(
        "MenuBar.css",
        "TopBar.css",
        "WindowControls.css",
        "StatusBar.css",
        "TextEditor.css",
        "NewFileWindow.css",
        "Settings.css",
        "LeftBar.css",
        "FileTree.css",
        "Window.css"
    )

    /**
     * Applies all known CSS files to a scene. This is the primary method for styling the main application window.
     */
    fun applyAllStyles(scene: Scene) {
        cssFiles.forEach { cssFile ->
            applySingleStyle(scene, cssFile)
        }
        logger.info("Applied all ${cssFiles.size} application CSS styles to the scene.")
    }


    /**
     * Apply specific CSS files to a scene
     */
    fun applyStyles(scene: Scene, vararg styleNames: String) {
        styleNames.forEach { styleName ->
            val cssFile = if (styleName.endsWith(".css")) styleName else "$styleName.css"
            applySingleStyle(scene, cssFile)
        }
        logger.debug("Applied ${styleNames.size} CSS styles to scene: ${styleNames.joinToString()}")
    }

    /**
     * Apply a single CSS file to a scene
     */
    private fun applySingleStyle(scene: Scene, cssFile: String) {
        try {
            val cssUrl = findCssResource(cssFile)
            if (cssUrl != null) {
                scene.stylesheets.add(cssUrl.toExternalForm())
                logger.debug("Loaded CSS: $cssFile")
            } else {
                logger.warn("CSS file not found: $cssFile")
            }
        } catch (e: Exception) {
            logger.error("Failed to load CSS file: $cssFile", e)
        }
    }

    /**
     * Get the external form URL of a CSS file by searching in predefined paths.
     */
    fun getCSSUrl(cssFileName: String): String? {
        return try {
            val cssFile = if (cssFileName.endsWith(".css")) cssFileName else "$cssFileName.css"
            val cssUrl = findCssResource(cssFile)

            if (cssUrl != null) {
                logger.debug("Retrieved CSS URL for: $cssFile")
                cssUrl.toExternalForm()
            } else {
                logger.warn("CSS file not found: $cssFile")
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to get CSS URL for: $cssFileName", e)
            null
        }
    }

    /**
     * Apply CSS to a component's stylesheet list
     * Use this for individual component
     */
    fun applyToComponent(stylesheets: MutableList<String>, vararg styleNames: String) {
        styleNames.forEach { styleName ->
            getCSSUrl(styleName)?.let { url ->
                stylesheets.add(url)
            }
        }
        logger.debug("Applied ${styleNames.size} CSS styles to component: ${styleNames.joinToString()}")
    }

    /**
     * Check if all required CSS files are available
     */
    fun validateCSSFiles(): Boolean {
        var allValid = true
        cssFiles.forEach { cssFile ->
            if (findCssResource(cssFile) == null) {
                logger.error("Missing required CSS file: $cssFile")
                allValid = false
            }
        }

        if (allValid) {
            logger.debug("All CSS files validated successfully")
        } else {
            logger.warn("Some CSS files are missing - styling may be incomplete")
        }

        return allValid
    }

    /**
     * Apply specific popup window styles
     * Use this for individual popup windows
     */
    fun applyPopupStyles(scene: Scene, vararg styleNames: String) {
        applyStyles(scene, *styleNames)
        logger.debug("Applied popup styles to scene: ${styleNames.joinToString()}")
    }

    /**
     * Searches for a CSS file in the predefined search paths.
     * @param cssFile The simple name of the CSS file (e.g., "TopBar.css").
     * @return The [URL] of the found resource, or null if not found.
     */
    private fun findCssResource(cssFile: String): URL? {
        for (path in searchPaths) {
            val fullPath = "$CSS_BASE_PATH$path$cssFile"
            val resource = CSSManager::class.java.getResource(fullPath)
            if (resource != null) {
                return resource
            }
        }
        return null
    }

    /** //TODO: REMOVE
     * Parses CSS custom properties from loaded stylesheets
     * @param cssClass The CSS class to extract properties from
     * @param propertyName The custom property name (e.g., "-icon-color")
     * @return The property value or null if not found
     */
    fun parseCSSCustomProperty(cssClass: String, propertyName: String): String? {
        return try {
            val cssUrl = findCssResource("WindowControls.css")
            if (cssUrl == null) {
                logger.warn("WindowControls.css not found for property parsing")
                return null
            }

            val cssContent = cssUrl.readText()

            extractCustomPropertyFromCSS(cssContent, cssClass, propertyName)
        } catch (e: Exception) {
            logger.error("Failed to parse CSS custom property: $propertyName", e)
            null
        }
    }

    /**
     * Extracts custom property value from CSS content
     */
    private fun extractCustomPropertyFromCSS(cssContent: String, cssClass: String, propertyName: String): String? {

        val classPattern = Regex("""\.${Regex.escape(cssClass)}\s*\{([^}]*)}""", RegexOption.MULTILINE)
        val propertyPattern = Regex("""${Regex.escape(propertyName)}\s*:\s*([^;]+);?""")

        val classMatch = classPattern.find(cssContent)
        if (classMatch != null) {
            val classContent = classMatch.groupValues[1]
            val propertyMatch = propertyPattern.find(classContent)
            if (propertyMatch != null) {
                val value = propertyMatch.groupValues[1].trim()
                logger.debug("Found CSS custom property $propertyName: $value")
                return value
            }
        }

        logger.debug("CSS custom property not found: $propertyName in class $cssClass")
        return null
    }

    /**
     * Extension function to read text from URL
     */
    private fun URL.readText(): String {
        return this.openStream().bufferedReader().use { it.readText() }
    }
}