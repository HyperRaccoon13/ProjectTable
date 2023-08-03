package com.github.erdragh.projecttable.block.entity

import com.github.erdragh.projecttable.block.ModBlocks
import com.github.erdragh.projecttable.client.screen.ProjectTableScreenHandler
import com.github.erdragh.projecttable.utils.ImplementedInventory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class ProjectTableBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlocks.PROJECT_TABLE_ENTITY_TYPE, pos, state), ImplementedInventory, ExtendedScreenHandlerFactory {

    private val inventorySize = 2 * 9
    // 9 extra slots for the crafting interface
    private val items = NonNullList.withSize(inventorySize + 9, ItemStack.EMPTY);
    override fun getItems(): NonNullList<ItemStack> {
        return items;
    }

    override fun getContainerSize(): Int {
        return items.size
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        ContainerHelper.loadAllItems(tag, items)
    }

    override fun saveAdditional(tag: CompoundTag) {
        ContainerHelper.saveAllItems(tag, items)
        return super.saveAdditional(tag)
    }

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? {
        return ProjectTableScreenHandler(i, inventory, this);
    }

    override fun getDisplayName(): Component {
        return TranslatableComponent(blockState.block.descriptionId)
    }

    override fun writeScreenOpeningData(player: ServerPlayer?, buf: FriendlyByteBuf?) {
        // currently, do nothing
    }
}
