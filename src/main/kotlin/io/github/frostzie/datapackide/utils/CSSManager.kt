package io.github.frostzie.datapackide.utils

import javafx.scene.Scene
import net.fabricmc.loader.api.FabricLoader

/**
 * Handles loading and applying stylesheets to scenes and components
 */
object CSSManager {
    private val logger = LoggerProvider.getLogger("CSSManager")

    private const val CSS_BASE_PATH = "/assets/datapack-ide/themes/"
    private val CUSTOM_CSS_PATH = FabricLoader.getInstance().configDir.toString() + "/datapack-ide/themes/"

    private val cssFiles = listOf(
        "MenuBar.css",
        "TitleBar.css",
        "StatusBar.css",
        "TextEditor.css",
        "NewFileWindow.css",
        "Settings.css",
        "LeftBar.css",
        "FileTree.css"
    )

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
            val cssUrl = CSSManager::class.java.getResource("$CSS_BASE_PATH$cssFile")
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
     * Get the external form URL of a CSS file
     */
    fun getCSSUrl(cssFileName: String): String? {
        return try {
            val cssFile = if (cssFileName.endsWith(".css")) cssFileName else "$cssFileName.css"
            val cssUrl = CSSManager::class.java.getResource("$CSS_BASE_PATH$cssFile")
            cssUrl?.toExternalForm().also {
                if (it == null) {
                    logger.warn("CSS file not found: $cssFile")
                } else {
                    logger.debug("Retrieved CSS URL: $cssFile")
                }
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
            val cssUrl = CSSManager::class.java.getResource("$CSS_BASE_PATH$cssFile")
            if (cssUrl == null) {
                logger.error("Missing CSS file: $cssFile")
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
}