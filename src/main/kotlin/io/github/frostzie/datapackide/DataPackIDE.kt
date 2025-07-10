package io.github.frostzie.datapackide

import io.github.frostzie.datapackide.utils.LoggerProvider
import net.fabricmc.api.ModInitializer

object DataPackIDE : ModInitializer {
	private val logger = LoggerProvider.getLogger("DataPack-IDE")

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
	}
}