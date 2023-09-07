package com.github.erdragh.projecttable.block.entity

import com.github.erdragh.projecttable.block.ModBlocks
import com.github.erdragh.projecttable.client.screen.ProjectTableScreenHandler
import com.github.erdragh.projecttable.config.ProjectTableConfig
import com.github.erdragh.projecttable.utils.DelegatedContainer
import com.github.erdragh.projecttable.utils.containerToNonNullList
import com.github.erdragh.projecttable.utils.nonNullListIntoContainer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.SimpleContainer
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

class ProjectTableBlockEntity(pos: BlockPos, state: BlockState) :
    BaseContainerBlockEntity(ModBlocks.PROJECT_TABLE_ENTITY_TYPE, pos, state), DelegatedContainer, WorldlyContainer {
    private val container: SimpleContainer = SimpleContainer((1 + ProjectTableConfig.EXTRA_STORAGE_ROWS.get()) * 9)

    init {
        container.addListener { setChanged() }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        ContainerHelper.saveAllItems(tag, containerToNonNullList(container))
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        val list = NonNullList.withSize(container.containerSize, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(tag, list)
        nonNullListIntoContainer(container, list)
    }

    override fun stillValid(player: Player): Boolean {
        return player.position().distanceToSqr(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5) < 5 * 5
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        if (ProjectTableConfig.STORAGE_INTERACTIONS.get()) {
            // include the indices for the crafting slots only if they're actually enabled in the config
            return if (ProjectTableConfig.INSERT_INTO_GRID.get()) IntArray(container.containerSize) { it } else IntArray(container.containerSize - 9) { it + 9 }
        }
        return IntArray(0)
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        if (!ProjectTableConfig.STORAGE_INTERACTIONS.get()) return false;
        val isCraftingSlot = index < 9
        return if (isCraftingSlot) {
            // you can only place items into the crafting grid from above and if it's enabled in the config
            ProjectTableConfig.INSERT_INTO_GRID.get() && (direction?.equals(Direction.UP) == true)
        } else {
            // if placing items into the grid is enabled you can't insert into other slots from the top
            if (ProjectTableConfig.INSERT_INTO_GRID.get()) direction?.equals(Direction.UP) != true else true
        }
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        if (!ProjectTableConfig.STORAGE_INTERACTIONS.get()) return false;
        // you can't take items out of the crafting slots ever, easily leads to dupes,
        // see Issue 16 (https://github.com/Erdragh/ProjectTableRefabricated/issues/16)
        return index >= 9
    }

    override fun getDelegateContainer(): Container {
        return container
    }

    override fun getDefaultName(): Component {
        return TranslatableComponent(blockState.block.descriptionId)
    }

    override fun createMenu(containerId: Int, inventory: Inventory): AbstractContainerMenu {
        return ProjectTableScreenHandler(containerId, inventory, this, ContainerLevelAccess.create(inventory.player.level, blockPos))
    }
}
