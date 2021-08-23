package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.utils.exposeAccessToPublic
import net.fabricmc.loom.configuration.providers.minecraft.ManifestVersion
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta
import org.gradle.api.Project
import java.io.File
import java.net.URL

object MinecraftProvider {

    private fun Project.provideMinecraftVersionsJson(): File {
        return getCachedFile("versionsManifest.json", false) {
            logger.lifecycle("$loggerPrefix - Fetching versions manifest")
            it.writeBytes(URL(VERSION_MANIFESTS).readBytes())
        }
    }

    internal fun provideGameVersionMeta(project: Project): MinecraftVersionMeta {
        val extension = project.lightCraftExtension
        val versionsJsonFile = project.provideMinecraftVersionsJson()
        val versionsManifest = objectMapper.readValue<ManifestVersion>(versionsJsonFile)

        val gameVersionManifestVersion =
            versionsManifest.versions.firstOrNull { it.id.equals(extension.clientVersion.friendlyName, true) }
                ?: throw Exception("Unable to find a vanilla manifest for version ${extension.clientVersion.friendlyName}")


        val gameVersionMetaFile = project.getCachedFile("versionManifest.json") {
            project.logger.lifecycle("$loggerPrefix - Fetching version meta manifest for ${extension.clientVersion.friendlyName}")
            it.writeBytes(URL(gameVersionManifestVersion.url).readBytes())
        }

        return objectMapper.readValue(gameVersionMetaFile)
    }

    fun provideMinecraftFile(project: Project): File {
        return project.getCachedFile("minecraft.jar") { jarFile ->
            project.logger.lifecycle("$loggerPrefix - Fetching client jar for Minecraft ${project.lightCraftExtension.clientVersion.friendlyName}")
            val gameVersionMeta = provideGameVersionMeta(project)

            jarFile.writeBytes(
                URL(
                    project.lightCraftExtension.customMinecraftJarUrl ?: gameVersionMeta.download("client").url
                ).readBytes()
            )

            // Remove the pesky META-INF signature file
            removeSignature(jarFile)

            // Go through each class and make sure that all the classes/fields/methods are publicly accessible
            jarFile.exposeAccessToPublic()
        }
    }
}