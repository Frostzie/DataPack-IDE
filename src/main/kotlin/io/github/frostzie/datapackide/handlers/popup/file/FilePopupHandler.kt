package io.github.frostzie.datapackide.handlers.popup.file

import io.github.frostzie.datapackide.events.RequestFileOverride
import io.github.frostzie.datapackide.events.RequestMoveConfirmation
import io.github.frostzie.datapackide.modules.popup.file.FilePopupModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

class FilePopupHandler(private val filePopupModule: FilePopupModule) {
    @SubscribeEvent
    fun onMoveFileRequested(event: RequestMoveConfirmation) {
        filePopupModule.showMoveConfirmation(event.sourcePath, event.targetPath)
    }

    @SubscribeEvent
    fun onFileOverrideRequested(event: RequestFileOverride) {
        filePopupModule.showFileOverrideDialog(event.sourcePath, event.targetPath)
    }
}
