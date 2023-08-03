package com.github.erdragh.projecttable

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

object ProjectTable : ModInitializer {
	const val MOD_ID = "projecttable"
	const val MOD_NAME = "Project Table"
    val logger = LoggerFactory.getLogger(MOD_NAME)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
	}

	fun id(path: String): ResourceLocation {
        return ResourceLocation(this.MOD_ID, path);
	}
}
