package io.github.frostzie.datapackide.handlers.popup.file

import io.github.frostzie.datapackide.events.RequestMoveConfirmation
import io.github.frostzie.datapackide.modules.popup.file.FilePopupModule
import io.github.frostzie.datapackide.settings.annotations.SubscribeEvent

class FilePopupHandler(private val filePopupModule: FilePopupModule) {
    @SubscribeEvent
    fun onMoveFileRequested(event: RequestMoveConfirmation) {
        filePopupModule.showMoveConfirmation(event.sourcePath, event.targetPath)
    }
}
