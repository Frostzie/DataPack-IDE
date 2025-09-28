package io.github.frostzie.datapackide.eventsOLD.handlersOLD

import io.github.frostzie.datapackide.config.AssetsConfig
import io.github.frostzie.datapackide.eventsOLD.EventBusOLD
import io.github.frostzie.datapackide.eventsOLD.UIAction
import io.github.frostzie.datapackide.eventsOLD.UIActionEvent
import io.github.frostzie.datapackide.utils.CSSManager
import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.stage.Stage

@Deprecated("Replacing with newer system")
/**
 * Handles UI-related actions such as window controls, opening dialogs, and toggling UI elements.
 */
class UIActionHandler(private val parentStage: Stage?) {
    companion object {
        private val logger = LoggerProvider.getLogger("UIActionHandler")
    }

    fun initialize() {
        EventBusOLD.register<UIActionEvent> { event ->
            logger.debug("Handling UI action: {}", event.action)
            handleUIAction(event)
        }
        logger.info("UIActionHandler initialized")
    }

    private fun handleUIAction(event: UIActionEvent) {
        when (event.action) {
            UIAction.RELOAD_STYLES -> reloadStyles()
            UIAction.RESET_STYLES_TO_DEFAULT -> resetStylesToDefault()
        }
    }

    private fun reloadStyles() {
        val scenesToReload = listOfNotNull(parentStage?.scene)
        CSSManager.reloadAllStyles(*scenesToReload.toTypedArray())
    }

    private fun resetStylesToDefault() {
        AssetsConfig.forceTransferAllAssets()
        val scenesToReload = listOfNotNull(parentStage?.scene)
        CSSManager.reloadAllStyles(*scenesToReload.toTypedArray())
    }
}