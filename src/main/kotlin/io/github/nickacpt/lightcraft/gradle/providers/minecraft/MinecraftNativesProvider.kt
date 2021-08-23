package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.getCachedFile
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta
import org.gradle.api.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.net.URL

object MinecraftNativesProvider {

    fun provideNativesFolder(project: Project): File {
        return provideNativesFolder0(project).also {
            if (it.list()?.isEmpty() == true) {
                extractNatives(project)
            }
        }
    }

    private fun provideNativesFolder0(project: Project) = project.getCachedFile("natives").also {
        it.mkdirs()
    }

    private fun provideJarStoreNativesFolder(project: Project): File {
        return project.getCachedFile("natives-jarstore").also { it.mkdirs() }
    }

    private fun getNativeFileDirs(project: Project): Pair<File, File> {
        return provideNativesFolder0(project) to provideJarStoreNativesFolder(project)
    }

    @Throws(IOException::class)
    private fun extractNatives(project: Project) {
        val (nativesDir, jarStore) = getNativeFileDirs(project)

        nativesDir.mkdirs()
        for (library in project.getNativesForProject()) {
            val libJarFile: File = library.relativeFile(jarStore)
            project.getCachedFile(libJarFile) {
                it.writeBytes(URL(library.url).readBytes())
            }
            ZipUtil.unpack(libJarFile, nativesDir)
        }
    }


    private fun Project.getNativesForProject(): List<MinecraftVersionMeta.Download> =
        MinecraftProvider.provideGameVersionMeta(this).libraries
            ?.filter { it.hasNativesForOS() }
            ?.mapNotNull { it.classifierForOS() } ?: emptyList()

}