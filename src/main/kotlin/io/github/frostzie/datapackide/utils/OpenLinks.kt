package io.github.frostzie.datapackide.utils

import net.minecraft.util.Util
import java.net.URI

object OpenLinks {

    fun discordLink() {
        Util.getOperatingSystem().open(URI("https://discord.gg/qZ885qTvkx"))
    }

    fun gitHubLink() {
        Util.getOperatingSystem().open(URI("https://github.com/Frostzie/DataPack-IDE"))
    }

    fun reportBugLink() {
        Util.getOperatingSystem().open(URI("https://github.com/Frostzie/DataPack-IDE/issues"))
    }

    fun buyMeACoffeeLink() {
        Util.getOperatingSystem().open(URI("https://www.buymeacoffee.com/frostzie"))
    }

    fun modrinthLink() {
        Util.getOperatingSystem().open(URI("https://modrinth.com/mod/datapack-ide"))
    }
}