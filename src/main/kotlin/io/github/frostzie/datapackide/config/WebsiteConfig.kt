package io.github.frostzie.datapackide.config

import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Manages website configuration and file transfer from mod resources to config directory
 */
object WebsiteConfig {

    private val logger = LoggerProvider.getLogger("WebsiteConfig")

    val websiteDir: Path = ConfigManager.configDir.resolve("website")

    fun initialize() {
        logger.info("Initializing WebsiteConfig...")

        if (!websiteDir.toFile().exists()) {
            websiteDir.toFile().mkdirs()
            logger.info("Created website directory: $websiteDir")
        }

        transferWebsiteFiles()

        logger.info("WebsiteConfig initialization complete")
    }

    private fun transferWebsiteFiles() {
        val resourceFiles = listOf("index.html", "editor.css", "editor.js")

        resourceFiles.forEach { fileName ->
            try {
                val resourcePath = "/assets/datapack-ide/editor/$fileName"
                val inputStream = this::class.java.getResourceAsStream(resourcePath)

                if (inputStream != null) {
                    val targetFile = websiteDir.resolve(fileName)
                    Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING)
                    logger.info("Transferred $fileName to config directory")
                    inputStream.close()
                } else {
                    logger.error("Resource not found: $resourcePath")
                }
            } catch (e: Exception) {
                logger.error("Failed to transfer $fileName", e)
            }
        }
    }

    fun getWebsiteIndexPath(): Path {
        return websiteDir.resolve("index.html")
    }
}