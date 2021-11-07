package io.github.nickacpt.lightcraft.gradle.providers.minecraft

import io.github.nickacpt.lightcraft.gradle.*
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.maven

object MinecraftLibraryProvider {

    fun provideMinecraftLibraries(project: Project) {
        val usesLwjglCompat =
            project.lightCraftExtension.experimentalSettings.injectLwjgl2CompatibilityLayer

        val blacklistedDependencies = listOf("launchwrapper", "asm-all")
        val dependencyVersionOverrides = listOf("2.9.1-nightly-20130708-debug3" to "2.9.1")
        project.repositories.maven(LIBRARIES_BASE)
        project.repositories.maven(FABRICMC_LIBRARIES_BASE)
        project.repositories.maven(JITPACK_LIBRARIES_BASE)

        val versionInfo: MinecraftVersionMeta = MinecraftProvider.provideGameVersionMeta(project)

        for (library in versionInfo.libraries ?: emptyList()) {
            if (usesLwjglCompat && library.name.contains("lwjgl")) {
                continue
            }
            if (library.isValidForOS && !library.hasNatives() && library.artifact() != null) {
                if (library.name != null && blacklistedDependencies.none { library.name.contains(it, true) }) {
                    var finalName = library.name
                    dependencyVersionOverrides.forEach {
                        finalName = finalName.replace(it.first, it.second)
                    }

                    println("Added $finalName")
                    project.dependencies.add(
                        MINECRAFT_LIBRARY_CONFIGURATION,
                        finalName
                    )
                }
            }
        }

        // Provide a few updated dependencies
        arrayOf(
            mixinDependency,
            asmDependency,
            asmTreeDependency,
            asmUtilDependency,
            "javax.annotation:javax.annotation-api:1.3.2"
        ).forEach {
            project.dependencies.add(
                MINECRAFT_LIBRARY_CONFIGURATION,
                it
            )
        }

        if (usesLwjglCompat) {
            project.dependencies.add(
                MINECRAFT_LIBRARY_CONFIGURATION,
                lwjgl2CompatDependency
            )

            val lwjglNatives = "natives-windows"

            arrayOf(
                "lwjgl", "lwjgl-glfw", "lwjgl-openal", "lwjgl-opengl"
            ).forEach {
                project.dependencies.add(
                    JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME,
                    "org.lwjgl:$it:3.2.3:$lwjglNatives"
                )
                project.dependencies.add(
                    MINECRAFT_LIBRARY_CONFIGURATION,
                    "org.lwjgl:$it:3.2.3"
                )
            }
        }

        // Add LightCraft LaunchWrapper
        (project.dependencies.add(
            LAUNCH_WRAPPER_CONFIGURATION,
            launchWrapperDependency
        ) as ExternalModuleDependency).apply {
            this.exclude(module = "lwjgl")
            if (project.lightCraftExtension.clientVersion.shipsLog4J) {
                this.exclude(module = "log4j-core")
                this.exclude(module = "log4j-api")
            }
        }
    }
}