package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.utils.AccessExposerClassVisitor
import net.fabricmc.loom.configuration.providers.minecraft.ManifestVersion
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

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

            jarFile.writeBytes(URL(gameVersionMeta.download("client").url).readBytes())

            // Remove the pesky META-INF signature file
            removeSignature(jarFile)

            // Go through each class and make sure that all the classes/fields/methods are publicly accessible
            exposeClasses(jarFile)
        }
    }

    private fun exposeClasses(jarFile: File) {
        FileSystems.newFileSystem(jarFile.toPath()).use { inputFs ->
            Files.walkFileTree(inputFs.getPath("/"), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    if (file.fileName.toString().endsWith(".class")) {
                        val bytes = Files.readAllBytes(file)
                        val reader = ClassReader(bytes)
                        val writer = ClassWriter(0)

                        reader.accept(AccessExposerClassVisitor(writer), 0)

                        Files.deleteIfExists(file)
                        Files.write(
                            file,
                            writer.toByteArray()
                        )

                    }
                    return FileVisitResult.CONTINUE
                }
            })

        }
    }
}