package com.github.erdragh.projecttable.compat.rei

import com.github.erdragh.projecttable.block.ModBlocks
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.plugin.common.BuiltinPlugin

object ProjectTableREIPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry?) {
        registry?.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(ModBlocks.PROJECT_TABLE))
    }
}
