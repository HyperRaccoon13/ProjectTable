package com.github.erdragh.projecttable.client.screen

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot

fun initPlayerSlots(addSlotFun: (slot: Slot) -> Unit, inventory: Inventory, xOffset: Int, yOffset: Int) {
    // actual player inventory
    for (y in 0..2) for (x in 0..8) {
        addSlotFun(Slot(inventory, x + y * 9 + 9, xOffset + (x * 18), yOffset + (y * 18)))
    }
    // player hotbar
    for (x in 0..8) {
        addSlotFun(Slot(inventory, x, xOffset + (x * 18), yOffset + 58))
    }
}
