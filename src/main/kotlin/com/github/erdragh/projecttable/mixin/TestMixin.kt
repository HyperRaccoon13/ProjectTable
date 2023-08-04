package com.github.erdragh.projecttable.mixin

import net.minecraft.client.Minecraft
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject

@Mixin(Minecraft::class)
class TestMixin {
    @Inject(method = ["updateTitle"], at = [At("HEAD")], cancellable = true)
    fun ptUpdateTitle() {
        println("Fuck")
    }
}
