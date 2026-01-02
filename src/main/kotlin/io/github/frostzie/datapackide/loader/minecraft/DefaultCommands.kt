package io.github.frostzie.datapackide.loader.minecraft

import io.github.frostzie.datapackide.screen.MainApplication
import io.github.frostzie.datapackide.utils.CommandUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object DefaultCommands {
    private val Command = ClientCommandRegistrationCallback.EVENT

    fun registerCommands() {
        Command.register { a, _ ->
            val mainNode = a.register(CommandUtils.caseInsensitiveLiteral("datapack-ide").executes {
                MainApplication.showMainWindow()
                1
            })

            a.register(CommandUtils.caseInsensitiveLiteral("ide").redirect(mainNode))
        }
    }
}