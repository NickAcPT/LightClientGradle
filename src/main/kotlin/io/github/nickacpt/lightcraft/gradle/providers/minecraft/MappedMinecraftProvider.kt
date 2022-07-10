package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.providers.mappings.MinecraftMappingsProvider
import io.github.nickacpt.lightcraft.gradle.utils.RemapMappingFile
import io.github.nickacpt.lightcraft.gradle.utils.peformMiscAsmProcessing
import io.github.nickacpt.lightcraft.gradle.utils.provideDependency
import io.github.nickacpt.lightcraft.gradle.utils.remapJar
import org.gradle.api.Project
import java.io.File
import kotlin.io.path.ExperimentalPathApi

object MappedMinecraftProvider {
    fun provideMappedMinecraftDependency(project: Project): File {
        val extension = project.lightCraftExtension

        return project.provideDependency(
            LIGHTCRAFT_MINECRAFT_DEP_GROUP,
            LIGHTCRAFT_MINECRAFT_DEP_NAME,
            extension.clientVersion.friendlyName,
            file = provideMappedMinecraftFile(project)
        )
    }

    private fun provideMappedMinecraftFile(project: Project): File {
        val unmappedMinecraftJar = MinecraftJarModsProvider.provideJarModdedMinecraftJar(project)
        val finalMappingsFile = MinecraftMappingsProvider.provideMappings(project)

        return project.getCachedFile("minecraft-mapped.jar") { mappedMinecraftJar ->
            project.logger.lifecycle("$loggerPrefix - Remapping Minecraft ${project.lightCraftExtension.computeVersionName()}")

            mappedMinecraftJar.delete()
            remapJar(
                project,
                unmappedMinecraftJar,
                mappedMinecraftJar,
                RemapMappingFile(finalMappingsFile, MAPPING_SOURCE_NS, MAPPING_DEST_NS)
            )

            mappedMinecraftJar.peformMiscAsmProcessing(project.lightCraftExtension.clientVersion, fixParameters = true)
        }
    }
}