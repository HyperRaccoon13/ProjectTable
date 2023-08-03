package com.github.erdragh.projecttable

import com.github.erdragh.projecttable.block.ModBlocks
import com.github.erdragh.projecttable.item.ModItems
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ProjectTable : ModInitializer {
	private const val MOD_ID = "projecttable"
	private const val MOD_NAME = "Project Table"
    private val logger: Logger = LoggerFactory.getLogger(MOD_NAME)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        ModItems.initialize()
        ModBlocks.initialize()
        logger.info("Project Table initialized");
	}

	fun id(path: String): ResourceLocation {
        return ResourceLocation(this.MOD_ID, path);
	}
}
