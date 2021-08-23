package io.github.nickacpt.lightcraft.gradle

import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import java.io.File

open class LightCraftGradleExtension {
    var clientVersion = ClientVersion.V1_5_2

    var extraPostMappingFiles: MutableList<File> = mutableListOf()

    var extraPostMappingUrls: MutableList<String> = mutableListOf()

    var extraPreMappingFiles: MutableList<File> = mutableListOf()

    var extraPreMappingUrls: MutableList<String> = mutableListOf()

    var provideOptifineJarMod: Boolean = false
}
