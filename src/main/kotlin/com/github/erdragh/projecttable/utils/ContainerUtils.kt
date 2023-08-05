package com.github.erdragh.projecttable.utils

import com.github.erdragh.projecttable.ProjectTable
import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import kotlin.math.min

fun containerToNonNullList(container: Container): NonNullList<ItemStack> {
    val list = NonNullList.withSize(container.containerSize, ItemStack.EMPTY)

    for (i in 0..<container.containerSize) {
        list[i] = container.getItem(i)
    }

    return list
}

fun nonNullListIntoContainer(container: Container, nonNullList: NonNullList<ItemStack>) {
    for (i in 0..<min(nonNullList.size, container.containerSize)) {
        container.setItem(i, nonNullList[i])
    }
}
