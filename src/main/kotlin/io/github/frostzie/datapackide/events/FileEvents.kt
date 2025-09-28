package io.github.frostzie.datapackide.events

import java.nio.file.Path

class ChooseDirectory
data class DirectorySelected(val directoryPath: Path)

class NewFile
class DeleteFile
class RenameFile
class MoveFile

class CopyFile
class CutFile
class PasteFile

class CopyPath
class OpenWith