package com.github.erdragh.projecttable.compat.rei

import com.github.erdragh.projecttable.ProjectTable
import com.github.erdragh.projecttable.client.screen.ProjectTableScreenHandler
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider
import me.shedaniel.rei.plugin.common.BuiltinPlugin

object ProjectTableREIPluginServer : REIServerPlugin {
    override fun registerMenuInfo(registry: MenuInfoRegistry?) {
        ProjectTable.logger.info("Registering custom REI menu info")
        registry?.register(BuiltinPlugin.CRAFTING, ProjectTableScreenHandler::class.java, SimpleMenuInfoProvider.of(::ProjectTableMenuInfo))
    }
}
