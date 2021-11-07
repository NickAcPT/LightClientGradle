package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.getCachedFile
import io.github.nickacpt.lightcraft.gradle.lightCraftExtension
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta
import org.gradle.api.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.net.URL

object MinecraftNativesProvider {
    private val gameNativesDirectory = "game-natives${File.separatorChar}"

    fun provideNativesFolder(project: Project): File {
        return provideNativesFolder0(project).also {
            if (it.list()?.isEmpty() == true) {
                extractNatives(project)
            }
        }
    }

    private fun provideNativesFolder0(project: Project): File {
        val lwjgl3Suffix =
            if (project.lightCraftExtension.experimentalSettings.injectLwjgl2CompatibilityLayer) "-lwjgl3" else ""
        return project.getCachedFile("${gameNativesDirectory}natives$lwjgl3Suffix").also {
            it.mkdirs()
        }
    }

    private fun provideJarStoreNativesFolder(project: Project): File {
        return project.getCachedFile("${gameNativesDirectory}natives-jarstore").also { it.mkdirs() }
    }

    private fun getNativeFileDirs(project: Project): Pair<File, File> {
        return provideNativesFolder0(project) to provideJarStoreNativesFolder(project)
    }

    @Throws(IOException::class)
    private fun extractNatives(project: Project) {

        val (nativesDir, jarStore) = getNativeFileDirs(project)
        nativesDir.mkdirs()

        val usingLwjglCompatLayer =
            project.lightCraftExtension.experimentalSettings.injectLwjgl2CompatibilityLayer

        if (false && usingLwjglCompatLayer) {


            val lwjglNatives = "natives-windows"

            project.configurations.detachedConfiguration(
                *arrayOf(
                    "lwjgl", "lwjgl-glfw", "lwjgl-openal", "lwjgl-opengl"
                ).map {
                    println("org.lwjgl:$it:3.2.3:$lwjglNatives")
                    project.dependencies.create("org.lwjgl:$it:3.2.3:$lwjglNatives")
                }.toTypedArray()
            ).resolve().forEach { zipFile ->
                ZipUtil.iterate(zipFile) { `in`, zipEntry ->
                    if (zipEntry.name.endsWith(".dll")) {
                        project.getCachedFile(File(nativesDir, zipEntry.name.substringAfterLast('/'))) {
                            it.writeBytes(`in`.readAllBytes())
                        }
                    }
                }
            }

        }

        for (library in project.getNativesForProject()) {
            if (usingLwjglCompatLayer && library.path.contains("lwjgl")) continue
            val libJarFile: File = library.relativeFile(jarStore)
            project.getCachedFile(libJarFile) {
                it.writeBytes(URL(library.url).readBytes())
            }
            ZipUtil.unpack(libJarFile, nativesDir)
        }
    }


    private fun Project.getNativesForProject(): List<MinecraftVersionMeta.Download> =
        MinecraftProvider.provideGameVersionMeta(this).libraries?.filter { it.hasNativesForOS() }
            ?.mapNotNull { it.classifierForOS() } ?: emptyList()

}