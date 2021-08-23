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
            project.logger.lifecycle("$loggerPrefix - Downloading assets for Minecraft ${project.lightCraftExtension.clientVersion.friendlyName}")
            downloadAssets(project, it)
        }
    }

    private fun downloadAssets(project: Project, assets: File) {
        val executor: ExecutorService =
            Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1))

        val index: AssetIndex = provideAssetIndex(project)

        val parent: Map<String, AssetObject> = index.objects.takeIf { it.isNotEmpty() } ?: index.fileMap
        for ((key, `object`) in parent) {
            val sha1: String = `object`.hash ?: ""
            val filename = if (index.isMapToResources) key else "objects" + File.separator + sha1.substring(
                0,
                2
            ) + File.separator + sha1
            if (filename.endsWith(".sha1")) continue
            val file = File(assets, filename)
            project.getCachedFile(file) {
                project.logger.lifecycle("$loggerPrefix - Downloading asset: $key")

                executor.execute {
                    val assetName = arrayOf(key)
                    val end = assetName[0].lastIndexOf("/") + 1
                    if (end > 0) {
                        assetName[0] = assetName[0].substring(end)
                    }
                    it.writeBytes(
                        URL(
                            RESOURCES_BASE + sha1.substring(
                                0,
                                2
                            ) + "/" + sha1
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

    private fun provideAssetIndexFile(project: Project): File {
        return project.getCachedFile("assetIndex.json") {
            project.logger.lifecycle("$loggerPrefix - Downloading asset index for Minecraft ${project.lightCraftExtension.clientVersion.friendlyName}")
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