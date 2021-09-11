@file:Suppress("LeakingThis")

package io.github.nickacpt.lightcraft.gradle.tasks.impl

import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.LightCraftConfigurations.launchWrapperConfiguration
import io.github.nickacpt.lightcraft.gradle.LightCraftConfigurations.minecraftLibraryConfiguration
import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MappedMinecraftProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftAssetsProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftNativesProvider
import io.github.nickacpt.lightcraft.gradle.utils.getMixinFiles
import io.github.nickacpt.lightcraft.gradle.utils.resolveClasspathAsPath
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import java.io.File

open class RunClientTask : JavaExec() {

    init {
        group = LIGHTCRAFT_TASK_GROUP

        val extension = project.lightCraftExtension
        setupClasspath()
        setupWorkingDirectory()
        setupMainClassAndArguments(extension)
    }

    private fun setupClasspath() {
        val jarsToDepend = mutableListOf<File>()

        // Depend on the Minecraft jar that is our dependency
        jarsToDepend += MappedMinecraftProvider.provideMappedMinecraftDependency(project)

        // Setup Task to depend on the jar task
        val jarTask = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME)
        dependsOn(jarTask)

        // Depend on the Jars that were outputted by our project
        jarsToDepend += jarTask.outputs.files.files

        // Depend on LaunchWrapper
        jarsToDepend += project.launchWrapperConfiguration.resolve()

        // Depend on the Minecraft libraries
        jarsToDepend += project.minecraftLibraryConfiguration.resolve()

        // Depend on the project's dependencies
        jarsToDepend += project.resolveClasspathAsPath().map { it.toFile() }

        // Finally, set up our dependency on these files
        classpath(*jarsToDepend.distinct().toTypedArray())
    }

    private fun setupWorkingDirectory() {
        workingDir = File(project.projectDir, "run").also { it.mkdirs() }
    }

    private fun setupMainClassAndArguments(extension: LightCraftGradleExtension) {
        // Launch with LaunchWrapper
        mainClass.set("net.minecraft.launchwrapper.Launch")

        setupJvmLaunchArguments(extension)

        setupGameLaunchArguments(extension)
    }

    private fun setupJvmLaunchArguments(extension: LightCraftGradleExtension) {
        val jvmLaunchArguments = mutableListOf<Pair<String, String>>()

        // Tell LaunchWrapper that we are launching Mixins on the client side
        jvmLaunchArguments += LAUNCHWRAPPER_MIXIN_SIDE_PROP to MIXIN_SIDE_CLIENT

        // Provide Minecraft with lwjgl natives
        jvmLaunchArguments += JVM_LIBRARY_PATH_PROP to "\"${MinecraftNativesProvider.provideNativesFolder(project)}\""

        // Tell LaunchWrapper what Minecraft class we are launching
        jvmLaunchArguments += LAUNCHWRAPPER_MAIN_CLASS_PROP to extension.clientVersion.mainClass

        // Tell LaunchWrapper what the Minecraft class is
        jvmLaunchArguments += LAUNCHWRAPPER_GAME_CLASS_PROP to extension.clientVersion.gameClass

        // Tell Minecraft that we are launching under a development environment
        jvmLaunchArguments += LIGHTCRAFT_LAUNCH_DEV_ENV to "true"

        if (extension.launchSettings.enableMixinsDebug) {
            // Tell Sponge Mixin that we want to enable all debug features
            jvmLaunchArguments += MIXINS_DEBUG to "true"
        }

        // Add Sponge Mixin java agent for hotswap
        val mixinsJar = getSpongeMixinJar()
        val spongeMixinJavaAgent = "-javaagent:${mixinsJar.absolutePath}"

        // Finally, set up our task to use these arguments
        jvmArgs = jvmLaunchArguments.map { "-D${it.first}=${it.second}" } + spongeMixinJavaAgent
    }

    private fun getSpongeMixinJar(): File {
        val mixinsJarConfig = project.configurations.detachedConfiguration()
        mixinsJarConfig.dependencies.add(project.dependencies.create(mixinDependency))
        val mixinsJar = mixinsJarConfig.resolve().first()
        return mixinsJar
    }

    private fun setupGameLaunchArguments(extension: LightCraftGradleExtension) {
        val isOneDotSixOrHigher = extension.clientVersion.ordinal >= ClientVersion.V1_6_4.ordinal

        // Set up game launch arguments
        val launchArguments = mutableListOf<String>()

        // Provide Minecraft with version/profile information if needed
        if (isOneDotSixOrHigher) {
            launchArguments += "--version"
            launchArguments += project.name
        }

        // Provide our player's username
        if (isOneDotSixOrHigher) launchArguments += "--username"
        launchArguments += extension.launchSettings.playerName

        // Provide Minecraft session id
        launchArguments += "-"

        // Provide our game directory
        launchArguments += "--gameDir"
        launchArguments += "\"${workingDir.absolutePath}\""

        // Provide our game assets directory
        launchArguments += "--assetsDir"
        launchArguments += "\"${MinecraftAssetsProvider.provideMinecraftAssets(project).absolutePath}\""

        // Provide Vanilla LaunchWrapper tweaker
        launchArguments += "--tweakClass"
        launchArguments += "net.minecraft.launchwrapper.VanillaTweaker"

        // Provide our custom Mixin LaunchWrapper tweaker
        launchArguments += "--tweakClass"
        launchArguments += "org.spongepowered.asm.launch.LightCraftMixinTweaker"

        // Provide Mixin files to LaunchWrapper
        project.getMixinFiles().forEach { mixinFile ->
            launchArguments += "--mixin"
            launchArguments += mixinFile.name
        }

        // Add 1.7.10 (and higher) arguments
        if (isOneDotSixOrHigher) {
            // Provide Minecraft with access token
            launchArguments += "--accessToken"
            launchArguments += "0"

            // Provide Minecraft with default user properties
            launchArguments += "--userProperties"
            launchArguments += "{}"
        }

        // Finally, set up our task to use these arguments
        args = launchArguments
    }

}