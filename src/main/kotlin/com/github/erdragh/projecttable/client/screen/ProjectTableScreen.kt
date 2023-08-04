package com.github.erdragh.projecttable.client.screen

import com.github.erdragh.projecttable.ProjectTable
import com.github.erdragh.projecttable.mixin.RecipeBookComponentAccessor
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import org.spongepowered.asm.mixin.Mixin

class ProjectTableScreen(handler: ProjectTableScreenHandler, inventory: Inventory, title: Component) : RecipeUpdateListener,
    AbstractContainerScreen<ProjectTableScreenHandler>(handler, inventory, title) {
    companion object {
        private val texture: ResourceLocation = ProjectTable.id("textures/gui/projecttable.png")
        private val recipeButtonTexture = ResourceLocation("textures/gui/recipe_button.png")
    }
    private val recipeBook: RecipeBookComponent = RecipeBookComponent()
    private var narrow: Boolean = false

    private var lastRevision: Int = -1

    init {
        this.imageWidth = 176
        this.imageHeight = 215
        this.inventoryLabelY = imageHeight - 93
    }

    override fun init() {
        super.init()
        this.narrow = width < 379

        this.recipeBook.init(width, height, minecraft, narrow, menu)
        this.leftPos = recipeBook.updateScreenPosition(width, imageWidth)
        addRenderableOnly(recipeBook)

        setInitialFocus(recipeBook)

        addRenderableWidget(ImageButton(leftPos + 5, topPos + 34, 20, 18, 0, 0, 19, recipeButtonTexture) { widget ->
            recipeBook.initVisuals()
            recipeBook.toggleVisibility()
            this.leftPos = recipeBook.updateScreenPosition(width, imageWidth)
            widget.x = this.leftPos + 5
            widget.y = this.topPos + 34
        })
    }

    override fun containerTick() {
        if (lastRevision != menu.stateId) {
            lastRevision = menu.stateId
            if (recipeBook.isVisible) {
                (recipeBook as RecipeBookComponentAccessor).projecttable_updateStackedContents()
            }
        }
        recipeBook.tick()
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(poseStack)

        if (this.recipeBook.isVisible && narrow) {
            this.renderBg(poseStack, partialTick, mouseX, mouseY)
            recipeBook.render(poseStack, mouseX, mouseY, partialTick)
        } else {
            recipeBook.render(poseStack, mouseX, mouseY, partialTick)
            super.render(poseStack, mouseX, mouseY, partialTick)
            recipeBook.renderGhostRecipe(poseStack, this.leftPos, this.topPos, true, partialTick)
        }

        renderTooltip(poseStack, mouseX, mouseY)
        recipeBook.renderTooltip(poseStack, leftPos, topPos, mouseX, mouseY)
    }

    override fun renderBg(poseStack: PoseStack, partialTick: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
        RenderSystem.setShaderTexture(0, texture)
        val x = leftPos
        val y = (height - imageHeight) / 2

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight)
    }

    override fun renderLabels(poseStack: PoseStack, mouseX: Int, mouseY: Int) {
        font.draw(poseStack, title, ((imageWidth - font.width(title)) / 2).toFloat(), titleLabelY.toFloat(), 4210752)
        font.draw(poseStack, playerInventoryTitle, inventoryLabelX.toFloat(), inventoryLabelY.toFloat(), 4210752)
    }

    override fun isHovering(x: Int, y: Int, width: Int, height: Int, mouseX: Double, mouseY: Double): Boolean {
        return (!narrow || !recipeBook.isVisible) && super.isHovering(x, y, width, height, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (recipeBook.mouseClicked(mouseX, mouseY, button)) {
            focused = recipeBook
            true
        } else {
            if (narrow && recipeBook.isVisible) true else super.mouseClicked(mouseX, mouseY, button)
        }
    }

    override fun hasClickedOutside(
        mouseX: Double,
        mouseY: Double,
        guiLeft: Int,
        guiTop: Int,
        mouseButton: Int
    ): Boolean {
        val bl = mouseX < guiLeft || mouseY < guiTop || mouseX >= guiLeft + imageWidth || mouseY >= guiTop + imageHeight
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton) && bl
    }

    override fun slotClicked(slot: Slot?, slotId: Int, mouseButton: Int, type: ClickType) {
        super.slotClicked(slot, slotId, mouseButton, type)
        recipeBook.slotClicked(slot)
    }

    override fun recipesUpdated() {
        recipeBook.recipesUpdated()
    }

    override fun removed() {
        recipeBook.removed()
        super.removed()
    }

    override fun getRecipeBookComponent(): RecipeBookComponent {
        return recipeBook
    }
}
