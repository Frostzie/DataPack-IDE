package io.github.frostzie.datapackide.loader.fabric

import net.fabricmc.loader.api.FabricLoader

object ModVersion {
    val current: String by lazy {
        FabricLoader.getInstance()
            .getModContainer("datapack-ide")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }
}