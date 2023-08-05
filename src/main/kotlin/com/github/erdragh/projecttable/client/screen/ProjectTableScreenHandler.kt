package com.github.erdragh.projecttable.client.screen

import com.github.erdragh.projecttable.ProjectTable
import com.github.erdragh.projecttable.block.ModBlocks
import com.github.erdragh.projecttable.utils.GenericTypeChecker
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

    constructor(syncId: Int, inventory: Inventory) : this(
        syncId,
        inventory,
        SimpleContainer(
            9 + //crafting grid
                    2 * 9 // extra storage
        ),
        ContainerLevelAccess.NULL
    )

    constructor(
        syncId: Int,
        inventory: Inventory,
        container: Container,
        access: ContainerLevelAccess
    ) : super(ProjectTable.PROJECT_TABLE_SCREEN_HANDLER_TYPE, syncId) {
        this.access = access
        this.player = inventory.player
        this.container = container
        this.inputContainer = object : CraftingContainer(this, 3, 3) {
            override fun getContainerSize(): Int {
                return 9
            }

            override fun isEmpty(): Boolean {
                for (i in 0..<containerSize) {
                    if (!getItem(i).isEmpty) return false
                }
                return true
            }

            override fun getItem(slot: Int): ItemStack {
                return if (slot >= containerSize) ItemStack.EMPTY else container.getItem(slot)
            }

            override fun removeItemNoUpdate(slot: Int): ItemStack {
                if (slot >= containerSize) throw IndexOutOfBoundsException()
                return container.removeItemNoUpdate(slot)
            }

            override fun removeItem(slot: Int, amount: Int): ItemStack {
                if (slot >= containerSize) throw IndexOutOfBoundsException()
                val stack = container.removeItem(slot, amount)
                if (!stack.isEmpty) {
                    slotsChanged(this)
                }
                return stack
            }

            override fun setItem(slot: Int, stack: ItemStack) {
                if (slot >= containerSize) throw IndexOutOfBoundsException()
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
                for (i in 0..<containerSize) {
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
        for (y in 0..1) for (x in 0..8) {
            addSlot(Slot(container, x + y * 9 + 9, 8 + x * 18, 84 + y * 18))
        }

        initPlayerSlots(this::addSlot, inventory, 8, 133)

        updateResultContainer(syncId, player.level, player, inputContainer, resultContainer, this)
    }

    companion object {
        private const val GRID_END = 3 * 3 + 1
        private const val INVENTORY_END = GRID_END + 9 * 2
        private const val PLAYER_INVENTORY_END = INVENTORY_END + (9 * 4)

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
            updateResultContainer(stateId, level, player, inputContainer, resultContainer, this)
        }
    }

    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        for (i in 0..<container.containerSize) itemHelper.accountSimpleStack(container.getItem(i))
    }

    override fun handlePlacement(placeAll: Boolean, recipe: Recipe<*>, player: ServerPlayer) {
        if (!GenericTypeChecker<Recipe<CraftingContainer>>().checkType(recipe)) {
            ProjectTable.logger.error("recipe {} failed generic typecheck", recipe)
        }
        object : ServerPlaceRecipe<CraftingContainer>(this) {
            override fun moveItemToGrid(slotToFill: Slot, ingredient: ItemStack) {
                // scan storage, but not grid
                for (i in 9..<container.containerSize) {
                    var stackThatMayFit = container.getItem(i)
                    val notEmpty = !stackThatMayFit.isEmpty
                    val stackMatches = ItemStack.isSameItemSameTags(
                        ingredient,
                        stackThatMayFit
                    )
                    val notDamaged = !stackThatMayFit.isDamaged
                    val notEnchanted = !stackThatMayFit.isEnchanted
                    val noCustomName = !stackThatMayFit.hasCustomHoverName()
                    if (notEmpty && stackMatches && notDamaged && notEnchanted && noCustomName) {
                        stackThatMayFit = stackThatMayFit.copy()
                        if (stackThatMayFit.count > 1) {
                            container.removeItem(i, 1)
                        } else {
                            container.removeItemNoUpdate(i)
                        }

                        stackThatMayFit.count = 1
                        if (slotToFill.item.isEmpty) {
                            slotToFill.set(stackThatMayFit)
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
        var newStackAtIndex = ItemStack.EMPTY
        val clickedSlot = if (index >= slots.size) null else slots[index]
        if (clickedSlot != null && clickedSlot.hasItem()) {
            val stackInClickedSlot = clickedSlot.item
            newStackAtIndex = stackInClickedSlot.copy()

            // quick-transferring the result
            if (index == 0) {
                // if we quick-transfer slot 0, it's the result, so we need
                // to notify stuff that something was crafted
                access.evaluate { level, _ ->
                    stackInClickedSlot.item.onCraftedBy(stackInClickedSlot, level, player)
                }
                // try and quick transfer the item into the player inventory
                if (!this.moveItemStackTo(
                        stackInClickedSlot,
                        INVENTORY_END,
                        PLAYER_INVENTORY_END,
                        false
                    )
                ) {
                    return ItemStack.EMPTY
                }

                clickedSlot.onQuickCraft(stackInClickedSlot, newStackAtIndex)
                // quick-transferring from the players inventory (to the extra table storage)
            } else if (index in INVENTORY_END..<PLAYER_INVENTORY_END) {
                if (!this.moveItemStackTo(stackInClickedSlot, GRID_END, INVENTORY_END, false)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(stackInClickedSlot, INVENTORY_END, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY
            }

            if (stackInClickedSlot.isEmpty) {
                clickedSlot.set(ItemStack.EMPTY)
            } else {
                clickedSlot.setChanged()
            }

            if (stackInClickedSlot.count == newStackAtIndex.count) {
                return ItemStack.EMPTY
            }

            clickedSlot.onTake(player, stackInClickedSlot)
        }

        return newStackAtIndex
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
        return 1 + 9
    }

    override fun getRecipeBookType(): RecipeBookType {
        return RecipeBookType.CRAFTING
    }

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        return slotIndex != this.resultSlotIndex
    }
}
