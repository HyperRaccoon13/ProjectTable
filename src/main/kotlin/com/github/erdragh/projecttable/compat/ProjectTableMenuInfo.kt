package com.github.erdragh.projecttable.compat

import com.github.erdragh.projecttable.client.screen.ProjectTableScreenHandler
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay

class ProjectTableMenuInfo(display: DefaultCraftingDisplay<*>) : CustomMenuInfo<ProjectTableScreenHandler, DefaultCraftingDisplay<*>> {

    private val displayValue: DefaultCraftingDisplay<*> = display

    override fun getInputSlots(context: MenuInfoContext<ProjectTableScreenHandler, *, DefaultCraftingDisplay<*>>?): MutableIterable<SlotAccessor> {
        val slots: MutableList<SlotAccessor> = ArrayList()
        for (i in 0..8) {
            slots.add(SlotAccessor.fromSlot(context?.menu?.getSlot(i)))
        }
        return slots
    }

    override fun getInventorySlots(context: MenuInfoContext<ProjectTableScreenHandler, *, DefaultCraftingDisplay<*>>?): MutableIterable<SlotAccessor> {
        val slots: MutableList<SlotAccessor> = ArrayList()

        // 36 for player inventory
        val totalSize = context?.menu?.slots?.size ?: 0

        // first, add all inventory slots, ensures they are first for emptying the table
        for (i in 0..<totalSize) {
            slots.add(SlotAccessor.fromSlot(context?.menu?.getSlot(i)))
        }

        return slots
    }

    override fun getDisplay(): DefaultCraftingDisplay<*> {
        return displayValue
    }
}
