package io.github.nickacpt.lightcraft.gradle.providers.mappings

import io.github.nickacpt.lightcraft.gradle.MAPPING_SOURCE_NS
import io.github.nickacpt.lightcraft.gradle.getCachedFile
import io.github.nickacpt.lightcraft.gradle.lightCraftExtension
import io.github.nickacpt.lightcraft.gradle.loggerPrefix
import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MappingNsRenamer
import net.fabricmc.mappingio.adapter.MissingDescFilter
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.Project
import java.io.File
import java.net.URL

object MinecraftMappingsProvider {
    val mappingsDirectory = "mappings${File.separatorChar}"

    private fun provideDefaultMappingUrlsForVersion(version: ClientVersion): List<String> {
        // Mojang mappings are provided separately if available
        if (version.hasMojangMappings) return emptyList()

        val urlsList =
            mutableListOf("https://raw.githubusercontent.com/NickAcPT/LightCraftMappings/main/${version.friendlyName}/mappings-official-srg-named.tiny2?v=${System.currentTimeMillis()}")
        if (version.hasExtraMappings) {
            urlsList.add("https://raw.githubusercontent.com/NickAcPT/LightCraftMappings/main/${version.friendlyName}/mappings-extra.tinyv2?v=${System.currentTimeMillis()}")
        }
        return urlsList
    }

    private fun provideDefaultMappingForVersion(project: Project, version: ClientVersion): File {
        return project.getCachedFile("${mappingsDirectory}mappings-default-final.tinyv2") { finalFile ->
            project.logger.lifecycle("$loggerPrefix - Fetching default deobfuscation mappings for Minecraft ${project.lightCraftExtension.computeVersionName()}")

            // First, load all default mappings
            val mappingFiles = provideDefaultMappingUrlsForVersion(version).mapIndexed {i, url ->
                project.getCachedFile("${mappingsDirectory}mappings-default-$i.tinyv2") {
                    val mappingBytes = URL(url).readBytes()
                    it.writeBytes(mappingBytes)
                }
            }.toMutableList()

            if (version.hasMojangMappings) {
                mappingFiles.add(MojangMappingsProvider.provideMappingsFile(project))
            }

            // Then, once we have them loaded, merge them together to the final file
            mergeMappings(mappingFiles, finalFile)

            // Then, if needed, update the source namespace of these mappings
            updateSourceNamespaceForDefaultMappings(project, finalFile)
        }
    }

    private fun updateSourceNamespaceForDefaultMappings(project: Project, it: File) {
        val defaultMappingsSourceNamespace = project.lightCraftExtension.defaultMappingsSourceNamespace
        // No need to rename default mapping source namespace
        if (defaultMappingsSourceNamespace == MAPPING_SOURCE_NS) return
        val tree = MemoryMappingTree()

        // Load original mappings
        MappingReader.read(it.toPath(), tree)

        // Rename Namespace and write it back to original location
        MappingWriter.create(it.toPath(), MappingFormat.TINY_2).use {
            tree.accept(MappingNsRenamer(it, mutableMapOf(MAPPING_SOURCE_NS to defaultMappingsSourceNamespace)))
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

            mergeMappings(finalMappingsList, finalMappingsFile)
        }
    }

    private fun mergeMappings(inputMappings: List<File>, outputFile: File) {
        val finalTree = MemoryMappingTree()
        inputMappings.forEach {
            MappingReader.read(it.toPath(), finalTree)
        }

        outputFile.delete()
        MappingWriter.create(outputFile.toPath(), MappingFormat.TINY_2).use {
            // Fix missing names by propagating names from previous namespaces
            // Then, ignore all fields and methods that are missing a descriptor (since it's invalid Tiny V2)
            // Finally write the merged tree to a file
            finalTree.accept(MissingNamespacePropagator(finalTree, MissingDescFilter(it)))
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