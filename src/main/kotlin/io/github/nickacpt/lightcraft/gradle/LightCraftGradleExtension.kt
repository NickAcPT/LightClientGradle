package io.github.nickacpt.lightcraft.gradle

import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import java.io.File

open class LightCraftGradleExtension {

    internal val launchSettings = LightCraftLaunchSettings()

    var clientVersion = ClientVersion.V1_5_2

    fun computeVersionName() =
        customMinecraftVersionName ?: clientVersion.friendlyName

    var customMinecraftVersionName: String? = null
    var customMinecraftJarUrl: String? = null

    //#region Pre Mappings
    var extraPreMappingUrls: MutableList<String> = mutableListOf()
    var extraPreMappingFiles: MutableList<File> = mutableListOf()
    //#endregion

    //#region Post Mappings
    var extraPostMappingUrls: MutableList<String> = mutableListOf()
    var extraPostMappingFiles: MutableList<File> = mutableListOf()
    //#endregion

    var defaultMappingsSourceNamespace = MAPPING_SOURCE_NS

    var provideOptifineJarMod: Boolean = false

    var provideOriginalLwjgl: Boolean = true

    fun launch(handler: LightCraftLaunchSettings.() -> Unit) {
        launchSettings.handler()
    }

}
