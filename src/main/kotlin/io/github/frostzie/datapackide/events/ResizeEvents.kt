package io.github.frostzie.datapackide.events

/**
 * Event for WindowControls
 */
class MainWindowClose
class MainWindowMinimize
class MainWindowMaximize
class MainWindowRestore
class MainResizeSides

data class MainWindowMaximizedStateChanged(val isMaximized: Boolean)