package io.github.nickacpt.lightcraft.gradle

import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MappedMinecraftProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftJarModsProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftLibraryProvider
import io.github.nickacpt.lightcraft.gradle.tasks.LightCraftGradleTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.add

class LightCraftGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.add(LightCraftGradleExtension::class, "lightcraft", LightCraftGradleExtension())

        // Setup Gradle configurations for libraries and jarmods
        LightCraftConfigurations.initConfigurations(project)

        project.evaluate {
            val extension = lightCraftExtension
            logger.lifecycle("$loggerPrefix - Minecraft ${extension.clientVersion.friendlyName}")

            // Setup Gradle configuration dependencies
            LightCraftConfigurations.setupConfigurationDeps(project)
            
            // Provide Optifine as JarMod if requested
            if (extension.provideOptifineJarMod) {
                MinecraftJarModsProvider.provideOptifineJarMod(project)
            }
            // Provide mapped Minecraft jar
            MappedMinecraftProvider.provideMappedMinecraftDependency(project)

            // Provide Minecraft libraries
            MinecraftLibraryProvider.provideMinecraftLibraries(project)

            LightCraftGradleTasks.setupTasks(project)
        }
    }
}