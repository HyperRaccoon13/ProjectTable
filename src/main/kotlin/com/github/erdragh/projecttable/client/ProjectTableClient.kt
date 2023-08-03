package com.github.erdragh.projecttable.client

import com.github.erdragh.projecttable.ProjectTable
import com.github.erdragh.projecttable.client.screen.ProjectTableScreen
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screens.MenuScreens

object ProjectTableClient : ClientModInitializer {
    override fun onInitializeClient() {
        MenuScreens.register(ProjectTable.PROJECT_TABLE_SCREEN_HANDLER_TYPE, ::ProjectTableScreen)
        ProjectTable.logger.info("{} client initialized", ProjectTable.MOD_NAME)
    }
}
