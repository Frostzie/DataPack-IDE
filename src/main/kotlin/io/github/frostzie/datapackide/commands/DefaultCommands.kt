package io.github.frostzie.datapackide.commands

import io.github.frostzie.datapackide.screen.JavaFXTestWindow
import io.github.frostzie.datapackide.utils.CommandUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object DefaultCommands {
    private val Command = ClientCommandRegistrationCallback.EVENT

    fun openMainScreen() {
        Command.register { a, _ ->
            val mainNode = a.register(CommandUtils.caseInsensitiveLiteral("datapack-ide").executes {
                JavaFXTestWindow.showTestWindow()
                1
            })

            a.register(CommandUtils.caseInsensitiveLiteral("ide").redirect(mainNode))
        }
    }
}