package com.github.erdragh.projecttable.client.screen

import com.github.erdragh.projecttable.ProjectTable
import com.github.erdragh.projecttable.config.ProjectTableConfig
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot

class ProjectTableScreen(handler: ProjectTableScreenHandler, inventory: Inventory, title: Component) :
    RecipeUpdateListener,
    AbstractContainerScreen<ProjectTableScreenHandler>(handler, inventory, title) {
    companion object {
        private val texture = ProjectTable.id("textures/gui/projecttable.png")
        private val recipeButtonTexture = ResourceLocation("textures/gui/recipe_button.png")
    }

    private val recipeBook = RecipeBookComponent()
    private var narrow = false

    private var lastRevision = -1

    init {
        imageWidth = 176
        imageHeight = 179 + ProjectTableConfig.EXTRA_STORAGE_ROWS.get() * 18
        inventoryLabelY = imageHeight - 93
        titleLabelX = 29
    }

    override fun init() {
        super.init()
        this.narrow = width < 379

        this.imageHeight = 179 + ProjectTableConfig.EXTRA_STORAGE_ROWS.get() * 18
        this.inventoryLabelY = imageHeight - 93

        minecraft?.let {
            this.recipeBook.init(width, height, it, narrow, menu)
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
    }

    override fun containerTick() {
        if (lastRevision != menu.stateId) {
            lastRevision = menu.stateId
            if (recipeBook.isVisible) {
                recipeBook.updateStackedContents()
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

        blit(poseStack, x, y, 0, 0, imageWidth, 83)

        for (i in 0..<ProjectTableConfig.EXTRA_STORAGE_ROWS.get()) {
            blit(poseStack, x, y + 83 + i * 18, 0, 83, imageWidth, 18)
        }

        blit(poseStack, x, y + 83 + ProjectTableConfig.EXTRA_STORAGE_ROWS.get() * 18, 0, 83 + 18, imageWidth, 96)
    }

    override fun renderLabels(poseStack: PoseStack, mouseX: Int, mouseY: Int) {
        font.draw(poseStack, title, titleLabelX.toFloat(), titleLabelY.toFloat(), 4210752)
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

    // I need to specify the maybe null here, because the method signature
    // is ill-defined in the mappings it seems. Sometimes the slot can be null,
    // which kotlin would detect and throw a NullPointerException
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
