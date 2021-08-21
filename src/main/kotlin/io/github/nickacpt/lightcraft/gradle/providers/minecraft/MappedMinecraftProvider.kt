package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.providers.mappings.MinecraftMappingsProvider
import io.github.nickacpt.lightcraft.gradle.utils.RemapMappingFile
import io.github.nickacpt.lightcraft.gradle.utils.provideDependency
import io.github.nickacpt.lightcraft.gradle.utils.remapJar
import org.gradle.api.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import net.fabricmc.mappingpoet.Main as MappingPoetMain

object MappedMinecraftProvider {
    @OptIn(ExperimentalPathApi::class)
    fun provideMappedMinecraft(project: Project) {
        val extension = project.lightCraftExtension

        val finalDependencyFile = project.provideDependency(
            "net.minecraft",
            "minecraft",
            extension.clientVersion.friendlyName,
            file = provideMappedMinecraftFile(project)
        )

        provideMinecraftJavadoc(
            project, File(
                finalDependencyFile.parent,
                "${finalDependencyFile.nameWithoutExtension}-javadoc.jar"
            )
        )
    }

    @ExperimentalPathApi
    fun provideMinecraftJavadoc(project: Project, javadocFile: File) {
        project.getCachedFile(javadocFile) {
            val tempLibrariesDir = createTempDirectory()
            val javadocOutputDir = createTempDirectory()

            val mappingsFile = MinecraftMappingsProvider.provideMappings(project)
            val mappedMinecraftFile = provideMappedMinecraftFile(project)
            MappingPoetMain.generate(
                mappingsFile.toPath(),
                mappedMinecraftFile.toPath(),
                javadocOutputDir,
                tempLibrariesDir
            )

            ZipUtil.pack(javadocOutputDir.toFile(), javadocFile)

            tempLibrariesDir.toFile().deleteRecursively()
            javadocOutputDir.toFile().deleteRecursively()
        }
    }


    fun provideMappedMinecraftFile(project: Project): File {
        val unmappedMinecraftJar = MinecraftJarModsProvider.provideJarModdedMinecraftJar(project)
        val finalMappingsFile = MinecraftMappingsProvider.provideMappings(project)

        return project.getCachedFile("minecraft-mapped.jar") { mappedMinecraftJar ->
            project.logger.lifecycle("$loggerPrefix - Remapping Minecraft ${project.lightCraftExtension.clientVersion.friendlyName}")

            mappedMinecraftJar.delete()
            remapJar(
                project,
                unmappedMinecraftJar,
                mappedMinecraftJar,
                RemapMappingFile(finalMappingsFile, MAPPING_SOURCE_NS, MAPPING_DEST_NS)
            )
        }
    }
}