package com.github.erdragh.projecttable.config

import net.minecraftforge.common.ForgeConfigSpec

object ProjectTableConfig {
    val SPEC: ForgeConfigSpec

    val EXTRA_STORAGE_ROWS: ForgeConfigSpec.IntValue

    init {
        val builder = ForgeConfigSpec.Builder()

        builder.comment("Project Table Config")

        EXTRA_STORAGE_ROWS = builder.comment("How many rows of extra storage should a project table have?")
            .defineInRange("extraStorageRows", 2, 0, 5)

        SPEC = builder.build()
    }
}
