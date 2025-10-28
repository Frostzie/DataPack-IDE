package io.github.frostzie.datapackide.config

import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Manages website configuration and file transfer from mod resources to config directory
 */
object WebsiteConfig {
    private val logger = LoggerProvider.getLogger("WebsiteConfig")

    //val websiteDir: Path = AssetsConfig.assetsDir.resolve("website")
    val websiteDir: Path = Paths.get(WebsiteConfig::class.java.getResource("/assets/datapack-ide/editor/")!!.toURI())

    fun initialize() {
/*
        logger.info("Initializing WebsiteConfig...")
        createDirectory()
        transferWebsiteFiles()
        logger.info("WebsiteConfig initialization complete")
    }

    private fun createDirectory() {
        if (!websiteDir.toFile().exists()) {
            websiteDir.toFile().mkdirs()
            logger.info("Created directory: $websiteDir")
        }
*/
    }
/*
    private fun transferWebsiteFiles() {
        listOf("index.html", "editor.css", "editor.js").forEach { fileName ->
            val resourcePath = "/assets/datapack-ide/editor/$fileName"
            AssetsConfig.transferAsset(resourcePath, websiteDir.resolve(fileName), "website file")
        }
    }
*/
    fun getWebsiteIndexPath(): Path {
        return websiteDir.resolve("index.html")
    }
}