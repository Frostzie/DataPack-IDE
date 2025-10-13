package io.github.frostzie.datapackide.utils

import javafx.scene.Scene
import net.fabricmc.loader.api.FabricLoader
import io.github.frostzie.datapackide.config.AssetsConfig
import java.net.URL
import java.nio.file.Files
import java.util.Base64

/**
 * Handles loading and applying stylesheets to scenes and components
 */
object CSSManager {
    private val logger = LoggerProvider.getLogger("CSSManager")

    private val CSS_CONFIG_PATH = FabricLoader.getInstance().configDir.resolve("datapack-ide/assets/styles/")

    private val resourceSearchPaths = listOf(
        ""
    )

    private val cssFiles = listOf<String>(
        "Debug.css",
        "Override.css"
    )

    /**
     * Applies all known CSS files to a scene. This is the primary method for styling the main application window.
     */
    fun applyAllStyles(scene: Scene) {
        logger.info("Applying all ${cssFiles.size} application CSS styles to the scene...")
        scene.stylesheets.clear()
        applyStyles(scene, *cssFiles.toTypedArray())
    }


    /**
     * Apply specific CSS files to a scene. This is now an internal helper.
     */
    private fun applyStyles(scene: Scene, vararg styleNames: String) {
        styleNames.forEach { styleName ->
            val cssFile = if (styleName.endsWith(".css")) styleName else "$styleName.css"
            loadCssContent(cssFile)?.let { content ->
                val dataUri = prepareCssForDataUri(content)
                scene.stylesheets.add(dataUri)
            }
        }
        logger.debug("Applied ${styleNames.size} CSS styles to scene using data URI: ${styleNames.joinToString()}")
    }

    /**
     * Apply specific popup window styles.
     * Use this for individual popup windows to avoid loading all application styles.
     */
    fun applyPopupStyles(scene: Scene, vararg styleNames: String) {
        applyStyles(scene, *styleNames)
        logger.debug("Applied popup styles to scene: ${styleNames.joinToString()}")
    }

    /**
     * Reloads all CSS files for one or more scenes efficiently.
     * It reads each CSS file only once and applies the result to all provided scenes.
     */
    fun reloadAllStyles(vararg scenes: Scene) {
        if (scenes.isEmpty()) return
        logger.info("Reloading all CSS styles for ${scenes.size} scene(s)...")

        val dataUris = cssFiles.mapNotNull { cssFile ->
            loadCssContent(cssFile)?.let { content ->
                prepareCssForDataUri(content)
            }
        }

        scenes.forEach { scene ->
            scene.stylesheets.clear()
            scene.stylesheets.addAll(dataUris)
        }

        logger.info("All styles reloaded successfully for ${scenes.size} scene(s).")
    }

    /**
     * Loads the raw string content of a CSS file from the highest-priority source.
     * Priority: User Config > Classpath Resource.
     */
    private fun loadCssContent(cssFile: String): String? {
        try {
            var cssBytes: ByteArray? = null
            var sourceDescription: String? = null

            val configFile = CSS_CONFIG_PATH.resolve(findCssInSubdirectory(cssFile))
            if (Files.isRegularFile(configFile)) {
                cssBytes = Files.readAllBytes(configFile)
                sourceDescription = "user config ($configFile)"
            }

            if (cssBytes == null) {
                findClasspathResource(cssFile)?.openStream()?.use { inputStream ->
                    cssBytes = inputStream.readAllBytes()
                    sourceDescription = "classpath resource"
                }
            }

            if (cssBytes != null) {
                logger.info("Loaded CSS content for: $cssFile from $sourceDescription")
                return String(cssBytes, Charsets.UTF_8)
            } else {
                logger.warn("CSS file not found during load: $cssFile")
                return null
            }
        } catch (e: Exception) {
            logger.error("Failed to load CSS content for file: $cssFile", e)
            return null
        }
    }

    /**
     * Takes raw CSS content, processes it, and returns a Base64-encoded data URI.
     */
    private fun prepareCssForDataUri(cssContent: String): String {
        var processedContent = cssContent
        val fontFile = AssetsConfig.getFontPath()
        if (Files.isRegularFile(fontFile)) {
            val fontUrl = fontFile.toUri().toString()
            processedContent = processedContent.replace(Regex("url\\((['\"])?.*?/DataPack-IDE\\.ttf\\1?\\)"), "url('$fontUrl')")
        }

        val encodedCss = Base64.getEncoder().encodeToString(processedContent.toByteArray(Charsets.UTF_8))
        return "data:text/css;base64,$encodedCss"
    }

    /**
     * Find CSS file in subdirectories, checking all search paths
     */
    private fun findCssInSubdirectory(cssFile: String): String {
        for (path in resourceSearchPaths) {
            val fullPath = if (path.isEmpty()) cssFile else "$path$cssFile"
            if (Files.isRegularFile(CSS_CONFIG_PATH.resolve(fullPath))) {
                return fullPath
            }
        }
        return cssFile
    }

    /**
     * Searches for a CSS file only within the mod's classpath resources.
     */
    private fun findClasspathResource(cssFile: String): URL? {
        for (path in resourceSearchPaths) {
            val fullPath = "/assets/datapack-ide/themes/$path$cssFile"
            val resource = CSSManager::class.java.getResource(fullPath)
            if (resource != null) return resource
        }
        return null
    }

    // TODO: Add CSS theme switching functionality for future use
    // This will allow switching between different CSS themes/variants
}