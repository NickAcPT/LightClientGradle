package io.github.nickacpt.lightcraft.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

internal object LightCraftConfigurations {

    val Project.jarModConfiguration: Configuration
        get() = configurations.getByName(JAR_MOD_CONFIGURATION)

    val Project.minecraftLibraryConfiguration: Configuration
        get() = configurations.getByName(MINECRAFT_LIBRARY_CONFIGURATION)

    val Project.upgradedMinecraftLibraryConfiguration: Configuration
        get() = configurations.getByName(UPGRADED_MINECRAFT_LIBRARY_CONFIGURATION)

    val Project.launchWrapperConfiguration: Configuration
        get() = configurations.getByName(LAUNCH_WRAPPER_CONFIGURATION)

    fun initConfigurations(project: Project) {
        project.configurations.create(JAR_MOD_CONFIGURATION)
        project.configurations.create(MINECRAFT_LIBRARY_CONFIGURATION)
        project.configurations.create(UPGRADED_MINECRAFT_LIBRARY_CONFIGURATION)
        project.configurations.create(LAUNCH_WRAPPER_CONFIGURATION)
    }

    fun setupConfigurationDeps(project: Project) {
        // Present Minecraft libraries as compile-time dependencies
        // These will get automatically ignored by ShadowJar
        project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
            .extendsFrom(project.minecraftLibraryConfiguration)
            .extendsFrom(project.upgradedMinecraftLibraryConfiguration)

        // Present LaunchWrapper as a runtime-only dependency
        // TODO: When adding ShadowJar support, exclude this from getting shaded
        project.configurations.getByName(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME)
            .extendsFrom(project.launchWrapperConfiguration)
    }

}