package com.github.erdragh.projecttable.compat.rei

import com.github.erdragh.projecttable.client.screen.ProjectTableScreenHandler
import me.shedaniel.rei.api.common.transfer.RecipeFinder
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.item.ItemStack

class ProjectTableMenuInfo(display: DefaultCraftingDisplay<*>) :
    SimpleGridMenuInfo<ProjectTableScreenHandler, DefaultCraftingDisplay<*>> {

    companion object {
        private const val RESULT_SLOT = 0
        private const val GRID_END = 3 * 3 + 1
        private const val INVENTORY_END = GRID_END + 9 * 2
        private const val PLAYER_INVENTORY_END = INVENTORY_END + (9 * 4)
    }

    private val displayValue: DefaultCraftingDisplay<*> = display

    override fun getCraftingResultSlotIndex(menu: ProjectTableScreenHandler?): Int {
        return RESULT_SLOT
    }

    override fun getCraftingWidth(menu: ProjectTableScreenHandler?): Int {
        return menu?.gridWidth ?: 0
    }

    override fun getCraftingHeight(menu: ProjectTableScreenHandler?): Int {
        return menu?.gridHeight ?: 0
    }

    override fun clearInputSlots(menu: ProjectTableScreenHandler?) {
        menu?.clearCraftingContent()
    }

    override fun getInputSlots(context: MenuInfoContext<ProjectTableScreenHandler, *, DefaultCraftingDisplay<*>>?): MutableIterable<SlotAccessor> {
        return context?.menu?.slots?.subList(1, GRID_END)?.map { SlotAccessor.fromSlot(it) }?.toMutableList()
            ?: ArrayList()
    }

    override fun getInventorySlots(context: MenuInfoContext<ProjectTableScreenHandler, *, DefaultCraftingDisplay<*>>?): MutableIterable<SlotAccessor> {
        return context?.menu?.slots?.subList(GRID_END, PLAYER_INVENTORY_END)?.map { SlotAccessor.fromSlot(it) }?.toMutableList()
            ?: ArrayList()
    }

    override fun populateRecipeFinder(
        context: MenuInfoContext<ProjectTableScreenHandler, *, DefaultCraftingDisplay<*>>?,
        finder: RecipeFinder?
    ) {
        context?.menu?.fillCraftSlotsStackedContents(object : StackedContents() {
            override fun accountSimpleStack(stack: ItemStack) {
                finder?.addNormalItem(stack)
            }
        })
    }

    override fun getDisplay(): DefaultCraftingDisplay<*> {
        return displayValue
    }
}
