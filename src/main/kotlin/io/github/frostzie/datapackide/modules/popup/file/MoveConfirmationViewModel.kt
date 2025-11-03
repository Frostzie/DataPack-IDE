package io.github.frostzie.datapackide.modules.popup.file

import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.events.MoveFile
import javafx.beans.property.SimpleStringProperty
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isDirectory

class MoveConfirmationViewModel(
    private val sourcePath: Path
) {
    val sourcePathLabel = SimpleStringProperty()
    val targetDirectory = SimpleStringProperty()
    val error = SimpleStringProperty()

    init {
        sourcePathLabel.set(buildSourcePathLabel())
    }

    fun confirm(): Boolean {
        error.set(null)
        try {
            val newTargetDirPath = Path.of(targetDirectory.get())
            if (Files.isDirectory(newTargetDirPath)) {
                val newTargetPath = newTargetDirPath.resolve(sourcePath.fileName)
                EventBus.post(MoveFile(sourcePath, newTargetPath))
                return true
            } else {
                //TODO: Add Error and Refactoring move.
                error.set("Invalid directory: Path does not exist or is not a directory.")
                return false
            }
        } catch (_: InvalidPathException) {
            error.set("Invalid path format.")
            return false
        }
    }

    private fun buildSourcePathLabel(): String {
        val label = if (sourcePath.isDirectory()) "Current directory:" else "Current file:"
        return "$label $sourcePath"
    }
}