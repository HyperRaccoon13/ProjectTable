package com.github.erdragh.projecttable.client.screen

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class ProjectTableScreen(handler: ProjectTableScreenHandler, inventory: Inventory, title: Component) : AbstractContainerScreen<ProjectTableScreenHandler>(handler, inventory, title) {
    private val texture: ResourceLocation = ResourceLocation("minecraft", "textures/gui/container/dispenser.png")
    override fun renderBg(poseStack: PoseStack, partialTick: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, texture)

        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight)
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBg(poseStack, partialTick, mouseX, mouseY)
        super.render(poseStack, mouseX, mouseY, partialTick)
        renderTooltip(poseStack, mouseX, mouseY)
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
    }
}
