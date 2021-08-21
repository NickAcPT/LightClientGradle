package io.github.nickacpt.lightcraft.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

internal object LightCraftConfigurations {

    val Project.jarModConfiguration: Configuration
        get() = configurations.getByName(JAR_MOD_CONFIGURATION)

    val Project.minecraftLibraryConfiguration: Configuration
        get() = configurations.getByName(MINECRAFT_LIBRARY_CONFIGURATION)

    fun initConfigurations(project: Project) {
        project.configurations.create(JAR_MOD_CONFIGURATION)
        project.configurations.create(MINECRAFT_LIBRARY_CONFIGURATION)
    }

    fun setupConfigurationDeps(project: Project) {
        project.configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
            .extendsFrom(project.configurations.getByName(MINECRAFT_LIBRARY_CONFIGURATION))
    }

}