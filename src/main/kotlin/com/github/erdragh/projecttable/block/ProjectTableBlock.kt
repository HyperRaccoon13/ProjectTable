package com.github.erdragh.projecttable.block

import com.github.erdragh.projecttable.block.entity.ProjectTableBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material

class ProjectTableBlock : BaseEntityBlock(
    Properties
        .of(Material.WOOD)
        .sound(SoundType.WOOD)
        .strength(1f, 1f)
) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return ProjectTableBlockEntity(pos, state)
    }

    // the render_type specifier in the block model json seems to not work in 1.18.2,
    // so I have to use this deprecated method to set the render shape
    @Deprecated("Deprecated in Java", ReplaceWith("render_type in model json"))
    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

}
