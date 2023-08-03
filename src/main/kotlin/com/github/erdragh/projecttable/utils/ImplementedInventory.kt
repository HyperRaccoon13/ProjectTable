package com.github.erdragh.projecttable.utils

import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

interface ImplementedInventory : Container {
    fun getItems(): NonNullList<ItemStack>

    fun size(): Int {
        return getItems().size
    }

    override fun isEmpty(): Boolean {
        return getItems().any {
            return it.isEmpty
        }
    }

    override fun getItem(slot: Int): ItemStack {
        return getItems()[slot]
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val result = ContainerHelper.removeItem(getItems(), slot, amount)
        if (!result.isEmpty) {
            setChanged()
        }
        return result
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(getItems(), slot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        getItems()[slot] = stack
        if (stack.count > stack.maxStackSize) {
            stack.count = stack.maxStackSize
        }
    }

    override fun clearContent() {
        getItems().clear()
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}
