package com.github.erdragh.projecttable.utils

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

interface DelegatedContainer : Container {
    fun getDelegateContainer(): Container

    override fun clearContent() {
        getDelegateContainer().clearContent()
    }

    override fun getContainerSize(): Int {
        return getDelegateContainer().containerSize
    }

    override fun isEmpty(): Boolean {
        return getDelegateContainer().isEmpty
    }

    override fun getItem(slot: Int): ItemStack {
        return getDelegateContainer().getItem(slot)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        return getDelegateContainer().removeItem(slot, amount)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return getDelegateContainer().removeItemNoUpdate(slot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        getDelegateContainer().setItem(slot, stack)
    }

    override fun getMaxStackSize(): Int {
        return getDelegateContainer().maxStackSize
    }
}
