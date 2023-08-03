package com.github.erdragh.projecttable.item

import com.github.erdragh.projecttable.ProjectTable
import net.minecraft.core.Registry
import net.minecraft.world.item.Item

object ModItems {
    fun initialize() {}
}

fun <T : Item> register(item: T, id: String): T {
    val itemId = ProjectTable.id(id);

    val registeredItem: T = Registry.register(Registry.ITEM, itemId, item);

    return registeredItem;
}
