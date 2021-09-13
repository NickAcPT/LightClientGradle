package io.github.nickacpt.lightcraft.gradle.providers.mappings

import io.github.nickacpt.lightcraft.gradle.MAPPING_DEST_NS
import io.github.nickacpt.lightcraft.gradle.MAPPING_SOURCE_NS
import io.github.nickacpt.lightcraft.gradle.getCachedFile
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftProvider
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.format.ProGuardReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.Project
import java.io.File
import java.net.URL

object MojangMappingsProvider {
    private const val MANIFEST_CLIENT_MAPPINGS = "client_mappings"

    fun provideMappingsFile(project: Project): File {
        return project.getCachedFile("${MinecraftMappingsProvider.mappingsDirectory}mappings-mojang-map.tinyv2") { file ->
            val clientMappingsFile = provideClientMappingsFile(project)

            val tree = MemoryMappingTree()
            clientMappingsFile.reader().use {
                // Read our proguard mappings into the tree
                ProGuardReader.read(it, MAPPING_DEST_NS, MAPPING_SOURCE_NS, tree)
            }

            MappingWriter.create(file.toPath(), MappingFormat.TINY_2).use { writer ->
                tree.accept(MappingSourceNsSwitch(InvalidMojangMapMappingVisitor(writer), MAPPING_SOURCE_NS))
            }
        }
    }

    private fun provideClientMappingsFile(project: Project): File {
        return project.getCachedFile("${MinecraftMappingsProvider.mappingsDirectory}mappings-mojang-map.tmp") {
            val clientMappings = MinecraftProvider.provideGameVersionMeta(project).download(MANIFEST_CLIENT_MAPPINGS)

            it.writeBytes(URL(clientMappings.url).readBytes())
        }
    }

}