package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.FABRICMC_LIBRARIES_BASE
import io.github.nickacpt.lightcraft.gradle.LIBRARIES_BASE
import io.github.nickacpt.lightcraft.gradle.MINECRAFT_LIBRARY_CONFIGURATION
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta
import org.gradle.api.Project
import org.gradle.kotlin.dsl.maven

object MinecraftLibraryProvider {

    private const val mixinsVersion = "0.9.2+mixin.0.8.2"
    private const val asmVersion = "9.1"
    const val mixinDependency = "net.fabricmc:sponge-mixin:$mixinsVersion"
    const val asmDependency = "org.ow2.asm:asm:$asmVersion"
    const val asmTreeDependency = "org.ow2.asm:asm-tree:$asmVersion"
    const val asmUtilDependency = "org.ow2.asm:asm-util:$asmVersion"

    fun provideMinecraftLibraries(project: Project) {

        val blacklistedDependencies = listOf("launchwrapper", "asm-all")
        project.repositories.maven(LIBRARIES_BASE)
        project.repositories.maven(FABRICMC_LIBRARIES_BASE)

        val versionInfo: MinecraftVersionMeta = MinecraftProvider.provideGameVersionMeta(project)

        for (library in versionInfo.libraries ?: emptyList()) {
            if (library.isValidForOS && !library.hasNatives() && library.artifact() != null) {
                if (library.name != null && blacklistedDependencies.none { library.name.contains(it, true) })
                    project.dependencies.add(
                        MINECRAFT_LIBRARY_CONFIGURATION,
                        library.name
                    )
            }
        }

        // Provide a few updated dependencies
        arrayOf(mixinDependency, asmDependency, asmTreeDependency, asmUtilDependency, "javax.annotation:javax.annotation-api:1.3.2").forEach {
            project.dependencies.add(
                MINECRAFT_LIBRARY_CONFIGURATION,
                it
            )
        }
    }
}