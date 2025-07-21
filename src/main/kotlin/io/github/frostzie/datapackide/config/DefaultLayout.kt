package io.github.frostzie.datapackide.config

import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Manages the ImGui layout file.
 *
 * It ensures that a `layout.ini` file exists in the config folder,
 * generating it from a default if necessary.
 */
object DefaultLayout {
    private val logger = LoggerProvider.getLogger("Default Layout")
    /**
     * Gets the path to the layout file, creating it from a default if it doesn't exist.
     *
     * @return The Path to the `layout.ini` file.
     */
    fun getAndEnsureLayoutIniPath(): Path {
        val configDir = FabricLoader.getInstance().configDir.resolve("DataPackIDE")
        val configFile = configDir.resolve("layout.ini")

        try {
            if (!Files.exists(configFile)) {
                logger.info("layout.ini not found. Generating default layout file at ${configFile.toAbsolutePath()}")
                configDir.createDirectories()
                configFile.writeText(DEFAULT_INI_CONTENT)
            }
        } catch (e: Exception) {
            logger.error("Error ensuring layout file exists: ${e.message}.")
        }
        return configFile
    }

    private val DEFAULT_INI_CONTENT = """
[Window][Central Dockspace Host]
Pos=60,30
Size=1800,987
Collapsed=0

[Window][File Explorer]
Pos=60,30
Size=450,987
Collapsed=0
DockId=0x00000001,1

[Window][Editor]
Pos=512,30
Size=1348,987
Collapsed=0
DockId=0x00000002,1

[Docking][Data]
DockSpace         ID=0x6A17A43A Window=0x41758494 Pos=60,30 Size=1800,987 Split=X
  DockNode        ID=0x00000001 Parent=0x6A17A43A SizeRef=450,987
  DockNode        ID=0x00000002 Parent=0x6A17A43A SizeRef=1348,987 CentralNode=1
""".trimIndent()
}