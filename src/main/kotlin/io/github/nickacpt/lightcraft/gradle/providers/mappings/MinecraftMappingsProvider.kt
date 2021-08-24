package io.github.nickacpt.lightcraft.gradle.providers.mappings

import io.github.nickacpt.lightcraft.gradle.getCachedFile
import io.github.nickacpt.lightcraft.gradle.lightCraftExtension
import io.github.nickacpt.lightcraft.gradle.loggerPrefix
import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MissingDescFilter
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.Project
import java.io.File
import java.net.URL

object MinecraftMappingsProvider {
    private val mappingsDirectory = "mappings${java.io.File.separatorChar}"

    private fun provideDefaultMappingUrlForVersion(version: ClientVersion): String {
        return "https://raw.githubusercontent.com/NickAcPT/LightCraftMappings/main/${version.friendlyName}/mappings-official-srg-named.tiny2?v=${System.currentTimeMillis()}"
    }

    private fun provideDefaultMappingForVersion(project: Project, version: ClientVersion): File {
        return project.getCachedFile("${mappingsDirectory}mappings-default.tinyv2") {
            project.logger.lifecycle("$loggerPrefix - Fetching default deobfuscation mappings for Minecraft ${project.lightCraftExtension.computeVersionName()}")
            val mappingBytes = URL(provideDefaultMappingUrlForVersion(version)).readBytes()
            it.writeBytes(mappingBytes)
        }
    }

    fun provideMappings(project: Project): File {
        val extension = project.lightCraftExtension
        return project.getCachedFile("${mappingsDirectory}mappings-final.tinyv2") { finalMappingsFile ->
            project.logger.lifecycle("$loggerPrefix - Merging deobfuscation mappings for Minecraft ${project.lightCraftExtension.computeVersionName()}")
            val preMappingsList =
                provideMappingsFileAndUrl(
                    project,
                    extension.extraPreMappingUrls,
                    extension.extraPreMappingFiles,
                    "pre"
                )

            val postMappingsList =
                provideMappingsFileAndUrl(
                    project,
                    extension.extraPostMappingUrls,
                    extension.extraPostMappingFiles,
                    "post"
                )

            val defaultMappingsFile = provideDefaultMappingForVersion(project, extension.clientVersion)
            val finalMappingsList = preMappingsList + defaultMappingsFile + postMappingsList

            val finalTree = MemoryMappingTree()
            finalMappingsList.forEach {
                MappingReader.read(it.toPath(), finalTree)
            }

            finalMappingsFile.delete()
            MappingWriter.create(finalMappingsFile.toPath(), MappingFormat.TINY_2).use {
                finalTree.accept(MissingDescFilter(it))
            }
        }
    }

    private fun provideMappingsFileAndUrl(
        project: Project,
        mappingUrls: MutableList<String>,
        mappingFiles: MutableList<File>,
        prefix: String
    ): List<File> {
        return mappingUrls.mapIndexed { i, it ->
            project.getCachedFile("${mappingsDirectory}${prefix}${File.separatorChar}mappings-$i.tinyv2") { outFile ->
                outFile.writeBytes(URL(it).readBytes())
            }
        } + mappingFiles
    }
}