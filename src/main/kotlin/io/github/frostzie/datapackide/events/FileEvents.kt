package io.github.frostzie.datapackide.events

import java.nio.file.Path

class ChooseDirectory
data class DirectorySelected(val directoryPath: Path)

class NewFile
class DeleteFile
class RenameFile
data class MoveFile(val sourcePath: Path, val targetPath: Path)
data class RequestMoveConfirmation(val sourcePath: Path, val targetPath: Path)

class CopyFile
class CutFile
class PasteFile

class CopyPath
class OpenWith

class SaveFile
class SaveAsFile
class SaveAllFiles

data class OpenFile(val path: Path)
