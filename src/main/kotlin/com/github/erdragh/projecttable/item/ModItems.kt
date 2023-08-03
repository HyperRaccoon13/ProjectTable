package com.github.erdragh.projecttable.item

import com.github.erdragh.projecttable.ProjectTable
import net.minecraft.core.Registry
import net.minecraft.world.item.Item

object ModItems {
    fun initialize() {}
}

fun <T : Item> register(item: T, id: String): T {
    return Registry.register(Registry.ITEM, ProjectTable.id(id), item)
}
