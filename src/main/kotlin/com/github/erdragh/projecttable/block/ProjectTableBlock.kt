package com.github.erdragh.projecttable.block

import com.github.erdragh.projecttable.block.entity.ProjectTableBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult

class ProjectTableBlock : BaseEntityBlock(
    Properties
        .of(Material.WOOD)
        .sound(SoundType.WOOD)
        .strength(1f, 1f)
) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return ProjectTableBlockEntity(pos, state)
    }

    // To quote the fabric discord's command `!!deprecated`:
    // In AbstractBlock (and its subclass Block), deprecated methods mean "override, not call".
    // This is because there is a corresponding method in BlockState you should call instead.
    // Overriding is fine - that is expected.
    @Deprecated("Deprecated in Java", ReplaceWith("render_type in model json"))
    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    // To quote the fabric discord's command `!!deprecated`:
    // In AbstractBlock (and its subclass Block), deprecated methods mean "override, not call".
    // This is because there is a corresponding method in BlockState you should call instead.
    // Overriding is fine - that is expected.
    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide()) {
            val menuProvider: MenuProvider? = state.getMenuProvider(level, pos)

            if (menuProvider != null) {
                player.openMenu(menuProvider)
            }
        }
        return InteractionResult.SUCCESS
    }

    // To quote the fabric discord's command `!!deprecated`:
    // In AbstractBlock (and its subclass Block), deprecated methods mean "override, not call".
    // This is because there is a corresponding method in BlockState you should call instead.
    // Overriding is fine - that is expected.
    @Deprecated("Deprecated in Java")
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (state.block != newState.block) {
            val entity: BlockEntity? = level.getBlockEntity(pos)
            if (entity is ProjectTableBlockEntity) {
                Containers.dropContents(level, pos, entity)
                level.updateNeighbourForOutputSignal(pos, this)
            }
        }
    }

    // To quote the fabric discord's command `!!deprecated`:
    // In AbstractBlock (and its subclass Block), deprecated methods mean "override, not call".
    // This is because there is a corresponding method in BlockState you should call instead.
    // Overriding is fine - that is expected.
    @Deprecated("Deprecated in Java", ReplaceWith("true"))
    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    // To quote the fabric discord's command `!!deprecated`:
    // In AbstractBlock (and its subclass Block), deprecated methods mean "override, not call".
    // This is because there is a corresponding method in BlockState you should call instead.
    // Overriding is fine - that is expected.
    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos))",
            "net.minecraft.world.inventory.AbstractContainerMenu"
        )
    )
    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos): Int {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos))
    }
}
