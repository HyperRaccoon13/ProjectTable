package com.github.erdragh.projecttable.client.screen

import com.github.erdragh.projecttable.ProjectTable
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class ProjectTableScreenHandler : AbstractContainerMenu {
    private val container: Container

    constructor(syncId: Int, inventory: Inventory, buf: FriendlyByteBuf) : this(syncId, inventory, SimpleContainer(9)) {
        // do something with buf
    }

    constructor(
        syncId: Int,
        inventory: Inventory,
        container: Container
    ) : super(ProjectTable.PROJECT_TABLE_SCREEN_HANDLER_TYPE, syncId) {
        checkContainerSize(container, 9);
        this.container = container;

        container.startOpen(inventory.player)

        // our inventory
        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(Slot(container, j + i * 3, 62 + j * 18, 17 + i * 18))
            }
        }
        // player inventory
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }
        // player hotbar
        for (i in 0..8) {
            this.addSlot(Slot(inventory, i, 8 + i * 18, 142))
        }
    }

    override fun stillValid(player: Player): Boolean {
        return this.container.stillValid(player)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val newStack = ItemStack.EMPTY
        val slot: Slot? = if (index >= this.slots.size) null else this.slots[index]

        if (slot != null && slot.hasItem()) {
            val originalStack = slot.item;

            if (index < this.container.containerSize) {
                if (!this.moveItemStackTo(originalStack, this.container.containerSize, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.container.containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
        }

        return newStack;
    }
}
