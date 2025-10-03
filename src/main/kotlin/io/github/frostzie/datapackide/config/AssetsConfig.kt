package io.github.frostzie.datapackide.config

import io.github.frostzie.datapackide.utils.LoggerProvider
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Manages mod assets transfer from resources to config directory
 */
object AssetsConfig {

    private val logger = LoggerProvider.getLogger("AssetsConfig")

    val assetsDir: Path = ConfigManager.configDir.resolve("assets")
    val fontsDir: Path = assetsDir.resolve("fonts")
    val stylesDir: Path = assetsDir.resolve("styles")

    fun initialize() {
        logger.info("Initializing AssetsConfig...")

        createDirectories()
        transferAllAssets()

        logger.info("AssetsConfig initialization complete")
    }

    private fun createDirectories() {
        listOf(assetsDir, fontsDir, stylesDir).forEach { dir ->
            try {
                if (Files.notExists(dir)) {
                    Files.createDirectories(dir)
                    logger.info("Created directory: $dir")
                }
            } catch (e: IOException) {
                logger.error("Failed to create directory: $dir", e)
            }
        }
    }

    /**
     * Deletes existing assets from the config folder and re-copies the default ones from the mod's resources.
     * This serves as a "reset to default" function.
     */
    fun forceTransferAllAssets() {
        logger.warn("Forcibly re-transferring all assets, this will overwrite any user modifications in the config/datapack-ide/assets/ directory!")
        try {
            if (Files.exists(stylesDir)) {
                stylesDir.toFile().deleteRecursively()
                logger.info("Deleted existing styles directory.")
            }
            if (Files.exists(fontsDir)) {
                fontsDir.toFile().deleteRecursively()
                logger.info("Deleted existing fonts directory.")
            }
            initialize()
            logger.info("Forced asset transfer complete. All assets have been reset to default.")
        } catch (e: Exception) {
            logger.error("Failed to perform forced asset transfer.", e)
        }
    }

    fun transferAllAssets() {
        transferFonts()
        transferStyles()
    }

    private fun transferFonts() {
        val resourcePath = "/assets/datapack-ide/themes/fonts/DataPack-IDE.ttf"
        transferAsset(resourcePath, fontsDir.resolve("DataPack-IDE.ttf"), "font file")
    }

    private fun transferStyles() {
        val styleFiles = mapOf(
            "MenuBar.css" to "bar/top-bar/",
            "TopBar.css" to "bar/top-bar/",
            "WindowControls.css" to "bar/top-bar/",
            "BottomBar.css" to "bar/",
            "TextEditor.css" to "main/",
            "NewFileWindow.css" to "popup/",
            "Settings.css" to "popup/",
            "LeftBar.css" to "bar/",
            "FileTree.css" to "main/",
            "Window.css" to ""
        )

        styleFiles.forEach { (fileName, subPath) ->
            val resourcePath = "/assets/datapack-ide/themes/styles/$subPath$fileName"
            val targetDir = if (subPath.isNotEmpty()) stylesDir.resolve(subPath.trimEnd('/')) else stylesDir
            transferAsset(resourcePath, targetDir.resolve(fileName), "style file")
        }
    }

    fun getFontPath(): Path {
        return fontsDir.resolve("DataPack-IDE.ttf")
    }

    internal fun transferAsset(resourcePath: String, targetFile: Path, assetType: String) {
        if (Files.exists(targetFile)) {
            logger.debug("Skipping transfer for existing {}: {}", assetType, targetFile.fileName)
            return
        }

        try {
            getAssetInputStream(resourcePath)?.use { inputStream ->
                Files.createDirectories(targetFile.parent)
                Files.copy(inputStream, targetFile)
                logger.info("Transferred missing $assetType: ${targetFile.fileName}")
            } ?: logger.error("Resource not found for $assetType: $resourcePath")
        } catch (e: Exception) {
            logger.error("Failed to transfer $assetType ${targetFile.fileName}", e)
        }
    }

    internal fun getAssetInputStream(resourcePath: String): java.io.InputStream? {
        return AssetsConfig::class.java.getResourceAsStream(resourcePath)
    }
}