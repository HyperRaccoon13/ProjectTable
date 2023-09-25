package com.github.erdragh.projecttable.config

import net.minecraftforge.common.ForgeConfigSpec

object ProjectTableConfig {
    val SPEC: ForgeConfigSpec

    val EXTRA_STORAGE_ROWS: ForgeConfigSpec.IntValue
    val STORAGE_INTERACTIONS: ForgeConfigSpec.BooleanValue
    val INSERT_INTO_GRID: ForgeConfigSpec.BooleanValue

    init {
        val builder = ForgeConfigSpec.Builder()

        builder.comment("Project Table Config")

        EXTRA_STORAGE_ROWS = builder.comment("How many rows of extra storage should a project table have?")
            .defineInRange("extraStorageRows", 2, 0, 5)

        STORAGE_INTERACTIONS = builder.comment("Whether the inventory of the block can be interacted with from the outside")
            .define("storageInteractions", true)

        INSERT_INTO_GRID = builder.comment("Whether inputting items into the block starts in the crafting grid or the extra storage")
            .define("insertIntoGrid", false)

        SPEC = builder.build()
    }
}
