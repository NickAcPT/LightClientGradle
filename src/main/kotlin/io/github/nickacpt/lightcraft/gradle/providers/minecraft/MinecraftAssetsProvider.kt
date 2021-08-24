package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.nickacpt.lightcraft.gradle.*
import net.fabricmc.loom.configuration.providers.minecraft.assets.AssetIndex
import net.fabricmc.loom.configuration.providers.minecraft.assets.AssetObject
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object MinecraftAssetsProvider {

    fun provideMinecraftAssets(project: Project): File {
        return project.getCachedFile(provideAssetsFolder0(project)) {
            project.logger.lifecycle("$loggerPrefix - Downloading assets for Minecraft ${project.lightCraftExtension.computeVersionName()}")
            downloadAssets(project, it)
        }
    }

    private fun downloadAssets(project: Project, assets: File) {
        val executor: ExecutorService =
            Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1))

        val index: AssetIndex = provideAssetIndex(project)

        val parent: Map<String, AssetObject> = index.objects.takeIf { it.isNotEmpty() } ?: index.fileMap
        for ((assetKey, assetObject) in parent) {
            val assetObjectHash: String = assetObject.hash ?: ""
            val filename =
                computeAssetFilename(index, assetKey, assetObjectHash)

            val file = File(assets, filename)
            project.getCachedFile(file) {
                project.logger.lifecycle("$loggerPrefix - Downloading asset: $assetKey")

                executor.execute {
                    it.writeBytes(
                        URL(
                            RESOURCES_BASE + assetObjectHash.substring(
                                0,
                                2
                            ) + "/" + assetObjectHash
                        ).readBytes()
                    )
                }
            }
        }

        //Wait for the assets to all download
        executor.shutdown()
        try {
            if (executor.awaitTermination(2, TimeUnit.HOURS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun computeAssetFilename(
        index: AssetIndex,
        assetKey: String,
        assetObjectHash: String
    ): String {
        return if (index.isMapToResources == true) {
            assetKey
        } else {
            "objects" + File.separator + assetObjectHash.take(2) + File.separator + assetObjectHash
        }
    }

    private fun provideAssetIndexFile(project: Project): File {
        return project.getCachedFile("assetIndex.json") {
            project.logger.lifecycle("$loggerPrefix - Downloading asset index for Minecraft ${project.lightCraftExtension.computeVersionName()}")
            it.writeBytes(URL(MinecraftProvider.provideGameVersionMeta(project).assetIndex.url).readBytes())
        }
    }

    private fun provideAssetIndex(project: Project): AssetIndex {
        return objectMapper.readValue(provideAssetIndexFile(project))
    }

    private fun provideAssetsFolder0(project: Project): File {
        if (project.lightCraftExtension.clientVersion.hasLegacyAssets) {
            return File(project.projectDir, "run" + File.separatorChar + "resources").also { it.mkdirs() }
        }
        return project.getCachedFile("assets").also { it.mkdirs() }
    }

}