package io.github.nickacpt.lightcraft.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

internal object LightCraftConfigurations {

    val Project.jarModConfiguration: Configuration
        get() = configurations.getByName(JAR_MOD_CONFIGURATION)

    val Project.minecraftLibraryConfiguration: Configuration
        get() = configurations.getByName(MINECRAFT_LIBRARY_CONFIGURATION)

    val Project.minecraftRemappedConfiguration: Configuration
        get() = configurations.getByName(MINECRAFT_REMAPPED_CONFIGURATION)

    val Project.upgradedMinecraftLibraryConfiguration: Configuration
        get() = configurations.getByName(UPGRADED_MINECRAFT_LIBRARY_CONFIGURATION)

    val Project.orionLauncherConfiguration: Configuration
        get() = configurations.getByName(ORION_LAUNCHER_CONFIGURATION)

    fun initConfigurations(project: Project) {
        project.configurations.create(JAR_MOD_CONFIGURATION)
        project.configurations.create(MINECRAFT_REMAPPED_CONFIGURATION)
        project.configurations.create(MINECRAFT_LIBRARY_CONFIGURATION)
        project.configurations.create(UPGRADED_MINECRAFT_LIBRARY_CONFIGURATION)
        project.configurations.create(ORION_LAUNCHER_CONFIGURATION)
    }

    fun setupConfigurationDeps(project: Project) {
        project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
            .extendsFrom(project.minecraftRemappedConfiguration)

        project.configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
            .extendsFrom(project.minecraftLibraryConfiguration)
            .extendsFrom(project.upgradedMinecraftLibraryConfiguration)
            .extendsFrom(project.jarModConfiguration)

        project.configurations.getByName(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME)
            .extendsFrom(project.orionLauncherConfiguration)
    }

}