package io.github.frostzie.datapackide.events

import java.nio.file.Path

class ReloadThemeEvent
class ThemeChangeEvent(val themeName: String)
class ImportThemeEvent
class OpenThemeEvent
class ThemeEditingSessionClosedEvent
class SaveFileEvent(val path: Path)