package com.github.erdragh.projecttable.client.screen

import com.github.erdragh.projecttable.ProjectTable
import com.github.erdragh.projecttable.block.ModBlocks
import com.github.erdragh.projecttable.utils.GenericTypeChecker
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.recipebook.ServerPlaceRecipe
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import kotlin.math.min

class ProjectTableScreenHandler : RecipeBookMenu<CraftingContainer> {
    private val container: Container
    private val inputContainer: CraftingContainer
    private val resultContainer: ResultContainer
    private val access: ContainerLevelAccess
    private val player: Player
    private val syncId: Int

    constructor(syncId: Int, inventory: Inventory) : this(
        syncId,
        inventory,
        SimpleContainer(9),
        ContainerLevelAccess.NULL
    )

    constructor(
        syncId: Int,
        inventory: Inventory,
        container: Container,
        access: ContainerLevelAccess
    ) : super(ProjectTable.PROJECT_TABLE_SCREEN_HANDLER_TYPE, syncId) {
        checkContainerSize(container, 9)
        this.access = access
        this.player = inventory.player
        this.container = container
        this.syncId = syncId
        this.inputContainer = object : CraftingContainer(this, 3, 3) {
            override fun getContainerSize(): Int {
                return 9
            }

            override fun isEmpty(): Boolean {
                for (i in 0..<size) {
                    if (!getItem(i).isEmpty) return false
                }
                return true
            }

            override fun getItem(slot: Int): ItemStack {
                return if (slot >= size) ItemStack.EMPTY else container.getItem(slot)
            }

            override fun removeItemNoUpdate(slot: Int): ItemStack {
                if (slot >= size) throw IndexOutOfBoundsException()
                return container.removeItemNoUpdate(slot)
            }

            override fun removeItem(slot: Int, amount: Int): ItemStack {
                if (slot >= size) throw IndexOutOfBoundsException()
                val stack = container.removeItem(slot, amount)
                if (!stack.isEmpty) {
                    slotsChanged(this)
                }
                return stack
            }

            override fun setItem(slot: Int, stack: ItemStack) {
                if (slot >= size) throw IndexOutOfBoundsException()
                container.setItem(slot, stack)
                slotsChanged(this)
            }

            override fun setChanged() {
                container.setChanged()
            }

            override fun stillValid(player: Player): Boolean {
                return container.stillValid(player)
            }

            override fun clearContent() {
                for (i in 0..<size) {
                    removeItemNoUpdate(i)
                }
            }
        }
        this.resultContainer = ResultContainer()

        // result slot
        addSlot(ResultSlot(player, inputContainer, resultContainer, 0, 124, 35))

        // crafting slots
        for (y in 0..2) for (x in 0..2) {
            addSlot(Slot(inputContainer, x + y * 3, 30 + x * 18, 17 + y * 18))
        }

        // extra project table storage
        for (y in 0..1) for (x in 0..2) {
            addSlot(Slot(container, x + y * 9 + 9, 8 + x * 18, 84 + y * 18))
        }

        initPlayerSlots(this::addSlot, inventory, 8, 133)

        updateResultContainer(syncId, player.level, player, inputContainer, resultContainer, this);
    }

    companion object {
        fun updateResultContainer(
            syncId: Int,
            level: Level,
            player: Player,
            craftingContainer: CraftingContainer,
            resultContainer: ResultContainer,
            menu: AbstractContainerMenu
        ) {
            if (!level.isClientSide()) {
                val serverPlayer = player as ServerPlayer
                var result = ItemStack.EMPTY

                val foundRecipe =
                    level.server?.recipeManager?.getRecipeFor(RecipeType.CRAFTING, craftingContainer, level)

                if (foundRecipe != null && foundRecipe.isPresent) {
                    val recipe = foundRecipe.get()
                    if (resultContainer.setRecipeUsed(level, serverPlayer, recipe)) {
                        result = recipe.assemble(craftingContainer)
                    }
                }

                resultContainer.setItem(0, result)
                serverPlayer.connection.send(
                    ClientboundContainerSetSlotPacket(
                        syncId,
                        menu.incrementStateId(),
                        0,
                        result
                    )
                )
            }
        }
    }

    override fun slotsChanged(container: Container) {
        access.evaluate { level, _ ->
            updateResultContainer(syncId, level, player, inputContainer, resultContainer, this);
        }
    }

    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        for (i in 0..<container.containerSize) {
            itemHelper.accountSimpleStack(container.getItem(i))
        }
    }

    override fun handlePlacement(placeAll: Boolean, recipe: Recipe<*>, player: ServerPlayer) {
        if (!GenericTypeChecker<Recipe<CraftingContainer>>().checkType(recipe)) {
            ProjectTable.logger.error("recipe {} failed generic typecheck", recipe)
        }
        object : ServerPlaceRecipe<CraftingContainer>(this) {
            override fun moveItemToGrid(slotToFill: Slot, ingredient: ItemStack) {
                // scan storage, but not grid
                for (i in 9..<container.containerSize) {
                    var stack = container.getItem(i)
                    if (!stack.isEmpty && ItemStack.matches(
                            ingredient,
                            stack
                        ) && !stack.isDamaged && !stack.isEnchanted && !stack.hasCustomHoverName()
                    ) {
                        stack = stack.copy()
                        if (stack.count > 1) {
                            container.removeItem(i, 1);
                        } else {
                            container.removeItemNoUpdate(i)
                        }

                        stack.count = 1
                        if (slotToFill.item.isEmpty) {
                            slotToFill.set(stack)
                        } else {
                            slotToFill.item.grow(1)
                        }
                    }
                }
                // handle player inventory
                super.moveItemToGrid(slotToFill, ingredient)
            }

            override fun clearGrid(bl: Boolean) {
                for (i in 0..<menu.size) {
                    if (menu.shouldMoveToInventory(i)) {
                        var stack = menu.getSlot(i).item.copy()
                        for (j in 9..<container.containerSize) {
                            if (stack.isEmpty) break
                            val cur = container.getItem(j)

                            if (cur.isEmpty) {
                                container.setItem(j, stack)
                                stack = ItemStack.EMPTY
                            } else if (ItemStack.isSameItemSameTags(stack, cur)) {
                                val amount = min(cur.maxStackSize - cur.count, stack.count)

                                stack.shrink(amount)
                                cur.grow(amount)
                            }
                        }
                        if (!stack.isEmpty) {
                            inventory.placeItemBackInInventory(stack, false)
                        }
                        menu.getSlot(i).set(stack)
                    }
                }
                menu.clearCraftingContent()
            }
        }.recipeClicked(
            player,
            // unchecked cast is not actually unchecked, see above
            recipe as Recipe<CraftingContainer>, placeAll
        )
    }

    override fun clearCraftingContent() {
        inputContainer.clearContent()
        resultContainer.clearContent()
    }

    override fun recipeMatches(recipe: Recipe<in CraftingContainer>): Boolean {
        return recipe.matches(inputContainer, player.level)
    }

    override fun removed(player: Player) {
        super.removed(player)
    }

    override fun stillValid(player: Player): Boolean {
        return AbstractContainerMenu.stillValid(access, player, ModBlocks.PROJECT_TABLE)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var stack = ItemStack.EMPTY
        val slot = if (index >= slots.size) null else slots[index]
        if (slot != null && slot.hasItem()) {
            val stack2 = slot.item
            stack = stack2.copy()

            if (index == 0) {
                access.evaluate { level, _ ->
                    stack2.item.onCraftedBy(stack2, level, player)
                }
                if (!this.moveItemStackTo(stack2, 28, 63, false)) {
                    return ItemStack.EMPTY
                }

                slot.onQuickCraft(stack2, stack)
            } else if (index in 28..63) {
                if (!this.moveItemStackTo(stack2, 10, 27, false)) {
                    return ItemStack.EMPTY
                }
            } else if (this.moveItemStackTo(stack2, 28, 63, false)) {
                return ItemStack.EMPTY
            }

            if (stack2.isEmpty) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (stack2.count == stack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, stack2)
        }

        return stack
    }

    override fun canTakeItemForPickAll(stack: ItemStack, slot: Slot): Boolean {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot)
    }

    override fun getResultSlotIndex(): Int {
        return 0
    }

    override fun getGridWidth(): Int {
        return this.inputContainer.width
    }

    override fun getGridHeight(): Int {
        return this.inputContainer.height
    }

    override fun getSize(): Int {
        return 10
    }

    override fun getRecipeBookType(): RecipeBookType {
        return RecipeBookType.CRAFTING
    }

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        return slotIndex != this.resultSlotIndex
    }
}
