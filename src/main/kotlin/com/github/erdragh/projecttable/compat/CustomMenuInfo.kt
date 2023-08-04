package com.github.erdragh.projecttable.compat

import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.transfer.RecipeFinderPopulator
import me.shedaniel.rei.api.common.transfer.info.MenuInfo
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler
import me.shedaniel.rei.api.common.transfer.info.simple.DumpHandler
import net.minecraft.world.inventory.AbstractContainerMenu

interface CustomMenuInfo<T : AbstractContainerMenu, D : Display> : MenuInfo<T, D> {
    fun getDumpHandler(): DumpHandler<T, D> {
        return DumpHandler { context, stackToDump ->
            val inventoryStacks = getInventorySlots(context)
            val emptySlot = DumpHandler.getEmptySlot(inventoryStacks)
            val nextSlot = DumpHandler.getOccupiedSlotWithRoomForStack(stackToDump, inventoryStacks) ?: emptySlot
            ?: return@DumpHandler false

            val stack = stackToDump.copy()
            stack.count += nextSlot.itemStack.count
            nextSlot.itemStack = stack
            return@DumpHandler true
        }
    }

    override fun getInputCleanHandler(): InputCleanHandler<T, D> {
        return InputCleanHandler { context ->
            for (gridStack in getInputSlots(context)) {
                InputCleanHandler.returnSlotsToPlayerInventory(context, getDumpHandler(), gridStack)
            }
        }
    }

    override fun getRecipeFinderPopulator(): RecipeFinderPopulator<T, D> {
        return RecipeFinderPopulator { context, finder ->
            for (inventoryStack in getInputSlots(context)) {
                finder.addNormalItem(inventoryStack.itemStack)
            }
        }
    }
}
