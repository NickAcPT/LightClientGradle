package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import io.github.nickacpt.lightcraft.gradle.utils.mergeZip
import io.github.nickacpt.lightcraft.gradle.utils.provideDependency
import org.gradle.api.Project
import java.io.File
import java.net.URL

object MinecraftJarModsProvider {

    private const val jarModsFolder = "jarMods"

    private fun provideOptifineJarUrl(version: ClientVersion): String {
        return when (version) {
            ClientVersion.V1_5_2 -> "https://www.dropbox.com/s/qj3ceu7qtvlw7ay/OptiFine_1.5.2_HD_U_D5%20%282%29.zip?dl=1"
            ClientVersion.V1_8_9 -> "https://www.dropbox.com/s/yab27828fuw4a0r/OptiFine_1.8.9_HD_U_M5_MOD.jar?dl=1"
        }
    }

    fun provideOptifineJarMod(project: Project) {
        val clientVersion = project.lightCraftExtension.clientVersion

        //Create JarMods folder
        prepareJarModsFolder(project)

        val optifineJar = project.getCachedFile(jarModsFolder + File.separatorChar + "jarmod-optifine.jar") {
            it.writeBytes(URL(provideOptifineJarUrl(clientVersion)).readBytes())
        }

        project.provideDependency(
            "net.optifine",
            "optifine",
            clientVersion.friendlyName,
            JAR_MOD_CONFIGURATION,
            optifineJar
        )
    }

    fun provideJarModdedMinecraftJar(project: Project): File {
        return project.getCachedFile("minecraft-merged.jar") { finalMergedJar ->
            project.logger.lifecycle("$loggerPrefix - Merging Jar Mods for Minecraft ${project.lightCraftExtension.computeVersionName()}")

            val unMappedMinecraftJar = MinecraftProvider.provideMinecraftFile(project)
            unMappedMinecraftJar.copyTo(finalMergedJar, true)

            project.configurations.getByName(JAR_MOD_CONFIGURATION).resolve().forEach { jarModFile ->
                mergeZip(jarModFile, finalMergedJar)
            }

            removeSignature(finalMergedJar)
        }
    }

    private fun prepareJarModsFolder(project: Project) {
        project.getCachedFile(jarModsFolder).mkdirs()
    }

}