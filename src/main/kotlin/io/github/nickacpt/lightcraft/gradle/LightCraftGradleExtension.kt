package io.github.nickacpt.lightcraft.gradle

import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import java.io.File

open class LightCraftGradleExtension {
    var clientVersion = ClientVersion.V1_5_2

    var extraMappings: MutableList<File> = mutableListOf()

    var provideOptifineJarMod: Boolean = false
}