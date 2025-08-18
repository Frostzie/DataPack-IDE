package io.github.frostzie.datapackide.utils

import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.*

/**
 * Utility class for managing datapack files within the datapack directory. //TODO: change to the actual datapack directory config is only for temp
 * Handles file creation, saving, loading, and organization.
 */
object FileUtils {

    private val logger = LoggerProvider.getLogger("FileUtils")

    private val configDir: Path = FabricLoader.getInstance().configDir.resolve("datapack_ide")
    private val datapacksDir: Path = configDir.resolve("datapacks")
    private val recentFilesFile: Path = configDir.resolve("recent_files.json")

    enum class FileType(val extension: String, val displayName: String) {
        JSON("json", "JSON"),
        MCFUNCTION("mcfunction", "MCFUNCTION"),
        MCMETA("mcmeta", "MCMETA"),
        NBT("nbt", "NBT")
    }

    data class FileInfo(
        val path: Path,
        val name: String,
        val type: FileType,
        val lastModified: LocalDateTime,
        val size: Long
    )

    init {
        initializeDirectories()
    }

    /**
     * Initialize the directory structure if it doesn't exist
     */
    private fun initializeDirectories() {
        try {
            listOf(configDir, datapacksDir).forEach { dir ->
                if (!dir.exists()) {
                    dir.createDirectories()
                    logger.info("Created directory: ${dir.absolutePathString()}")
                }
            }
        } catch (e: IOException) {
            logger.error("Failed to initialize directories", e)
        }
    }

    /**
     * Creates a new file with a specific name and content based on its type.
     *
     * @param baseName The desired name of the file, without extension.
     * @param type The type of file to create, which determines the default content and extension.
     * @return A [FileInfo] object if the file was created successfully, otherwise null (e.g., if it already exists).
     */
    fun createNewFile(baseName: String, type: FileType): FileInfo? {
        return try {
            val finalFileName = if (baseName.endsWith(".${type.extension}")) {
                baseName
            } else {
                "$baseName.${type.extension}"
            }
            val filePath = datapacksDir.resolve(finalFileName)

            if (filePath.exists()) {
                logger.warn("File already exists, creation aborted: $filePath")
                return null
            }

            val content = getDefaultContent(type)
            filePath.writeText(content)

            val fileInfo = FileInfo(
                path = filePath,
                name = finalFileName,
                type = type,
                lastModified = LocalDateTime.now(),
                size = filePath.fileSize()
            )

            logger.info("Created new file: ${filePath.absolutePathString()}")
            addToRecentFiles(fileInfo)
            fileInfo

        } catch (e: IOException) {
            logger.error("Failed to create new file with name '$baseName'", e)
            null
        }
    }

    /**
     * Save content to a file
     */
    fun saveFile(filePath: Path, content: String): Boolean {
        return try {
            filePath.parent?.createDirectories()

            filePath.writeText(content)

            logger.info("Saved file: ${filePath.absolutePathString()} (${content.length} characters)")

            if (filePath.startsWith(datapacksDir)) {
                val fileInfo = FileInfo(
                    path = filePath,
                    name = filePath.name,
                    type = detectFileType(filePath),
                    lastModified = LocalDateTime.now(),
                    size = filePath.fileSize()
                )
                addToRecentFiles(fileInfo)
            }

            true
        } catch (e: IOException) {
            logger.error("Failed to save file: ${filePath.absolutePathString()}", e)
            false
        }
    }

    /**
     * Save content to a new file //TODO: add file name popup
     */
    fun saveAsNewFile(content: String, type: FileType = FileType.JSON, baseName: String? = null): FileInfo? {
        return try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = baseName?.let { "${it}_$timestamp.${type.extension}" }
                ?: "datapack_file_$timestamp.${type.extension}"
            val filePath = datapacksDir.resolve(fileName)

            if (saveFile(filePath, content)) {
                FileInfo(
                    path = filePath,
                    name = fileName,
                    type = type,
                    lastModified = LocalDateTime.now(),
                    size = filePath.fileSize()
                )
            } else null

        } catch (e: Exception) {
            logger.error("Failed to save as new file", e)
            null
        }
    }

    /**
     * Load content from a file //TODO: make a popup to choose the file
     */
    fun loadFile(filePath: Path): String? {
        return try {
            if (!filePath.exists()) {
                logger.warn("File does not exist: ${filePath.absolutePathString()}")
                return null
            }

            val content = filePath.readText()
            logger.info("Loaded file: ${filePath.absolutePathString()} (${content.length} characters)")

            if (filePath.startsWith(datapacksDir)) {
                val fileInfo = FileInfo(
                    path = filePath,
                    name = filePath.name,
                    type = detectFileType(filePath),
                    lastModified = LocalDateTime.now(),
                    size = filePath.fileSize()
                )
                addToRecentFiles(fileInfo)
            }

            content
        } catch (e: IOException) {
            logger.error("Failed to load file: ${filePath.absolutePathString()}", e)
            null
        }
    }

    /**
     * Get all files in the datapacks directory
     */
    fun listDatapackFiles(): List<FileInfo> {
        return try {
            if (!datapacksDir.exists()) {
                return emptyList()
            }

            Files.walk(datapacksDir)
                .filter { it.isRegularFile() }
                .map { path ->
                    FileInfo(
                        path = path,
                        name = path.name,
                        type = detectFileType(path),
                        lastModified = LocalDateTime.ofEpochSecond(
                            Files.getLastModifiedTime(path).toInstant().epochSecond, 0,
                            java.time.ZoneOffset.UTC
                        ),
                        size = path.fileSize()
                    )
                }
                .sorted { a, b -> b.lastModified.compareTo(a.lastModified) }
                .toList()

        } catch (e: IOException) {
            logger.error("Failed to list datapack files", e)
            emptyList()
        }
    }

     /**
     * Get default content for a file type. //TODO: add some default content if needed, not sure what files have what so...
     */
    private fun getDefaultContent(type: FileType): String {
        return when (type) {
            FileType.JSON -> ""
            FileType.MCFUNCTION -> ""
            FileType.MCMETA -> ""
            FileType.NBT -> ""
        }
    }

    /**
     * Detect file type based on file extension and content
     */
    private fun detectFileType(filePath: Path): FileType {
        return when (filePath.extension.lowercase()) {
            "mcfunction" -> FileType.MCFUNCTION
            "mcmeta" -> FileType.MCMETA
            "nbt" -> FileType.NBT
            "json" -> {
                //TODO: Detect json type based on content
                //TODO: For safety detect executable files and disable them
                FileType.JSON
            }
            else -> FileType.JSON
        }
    }

    /**
     * Add file to recent files list
     */
    private fun addToRecentFiles(fileInfo: FileInfo) {
        // TODO: Implement recent files tracking using JSON
        logger.debug("Added to recent files: ${fileInfo.name}")
    }

    /**
     * Get the datapacks directory path
     */
    fun getDatapacksDirectory(): Path = datapacksDir

    /**
     * Check if a file exists in the datapacks directory
     */
    fun fileExists(fileName: String): Boolean {
        return datapacksDir.resolve(fileName).exists()
    }

    /**
     * Delete a file from the datapacks directory
     */
    fun deleteFile(filePath: Path): Boolean {
        return try {
            if (filePath.startsWith(datapacksDir) && filePath.exists()) {
                filePath.deleteExisting()
                logger.info("Deleted file: ${filePath.absolutePathString()}")
                true
            } else {
                logger.warn("Cannot delete file outside datapacks directory or file doesn't exist: ${filePath.absolutePathString()}")
                false
            }
        } catch (e: IOException) {
            logger.error("Failed to delete file: ${filePath.absolutePathString()}", e)
            false
        }
    }

    /**
     * Create a backup of a file before overwriting
     */
    fun createBackup(filePath: Path): Path? {
        return try {
            if (!filePath.exists()) return null

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val backupName = "${filePath.nameWithoutExtension}_backup_$timestamp.${filePath.extension}"
            val backupPath = filePath.parent.resolve(backupName)

            Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("Created backup: ${backupPath.absolutePathString()}")
            backupPath

        } catch (e: IOException) {
            logger.error("Failed to create backup for: ${filePath.absolutePathString()}", e)
            null
        }
    }
}