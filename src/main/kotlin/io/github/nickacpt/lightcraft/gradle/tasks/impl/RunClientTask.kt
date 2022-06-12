@file:Suppress("LeakingThis")

package io.github.nickacpt.lightcraft.gradle.tasks.impl

import io.github.nickacpt.lightcraft.gradle.*
import io.github.nickacpt.lightcraft.gradle.LightCraftConfigurations.jarModConfiguration
import io.github.nickacpt.lightcraft.gradle.LightCraftConfigurations.minecraftLibraryConfiguration
import io.github.nickacpt.lightcraft.gradle.LightCraftConfigurations.orionLauncherConfiguration
import io.github.nickacpt.lightcraft.gradle.LightCraftConfigurations.upgradedMinecraftLibraryConfiguration
import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import io.github.nickacpt.lightcraft.gradle.providers.mappings.MinecraftMappingsProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MappedMinecraftProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftAssetsProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftNativesProvider
import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MinecraftProvider
import io.github.nickacpt.lightcraft.gradle.utils.resolveClasspathAsPath
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.io.File

open class RunClientTask : JavaExec() {

    init {
        group = LIGHTCRAFT_TASK_GROUP

        val extension = project.lightCraftExtension
        setupClasspath(extension)
        setupWorkingDirectory()
        setupMainClassAndArguments(extension)
    }

    private fun setupClasspath(extension: LightCraftGradleExtension) {
        val jarsToDepend = mutableListOf<File>()

        // Depend on the Minecraft jar mods first
        jarsToDepend += project.jarModConfiguration.resolve()

        // Depend on the Minecraft jar that is our dependency
        jarsToDepend += if (extension.launchSettings.deobfuscateInDev)
            MinecraftProvider.provideMinecraftFile(project)
        else MappedMinecraftProvider.provideMappedMinecraftDependency(project)

        // Setup Task to depend on the jar task
        val jarTask = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME)
        dependsOn(jarTask)

        // Depend on the Jars that were outputted by our project
        jarsToDepend += jarTask.outputs.files.files

        // Depend on LaunchWrapper
        jarsToDepend += project.orionLauncherConfiguration.resolve()

        // Depend on the original Minecraft libraries
        jarsToDepend += project.minecraftLibraryConfiguration.resolve()

        // Depend on the upgraded Minecraft libraries
        jarsToDepend += project.upgradedMinecraftLibraryConfiguration.resolve()

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
        mainClass.set("io.github.orioncraftmc.launcher.Entrypoint")

        setupJvmLaunchArguments(extension)

        setupGameLaunchArguments(extension)
    }

    private fun setupJvmLaunchArguments(extension: LightCraftGradleExtension) {
        val jvmLaunchArguments = mutableListOf<Pair<String, String>>()

        // Provide Minecraft with lwjgl natives
        jvmLaunchArguments += JVM_LIBRARY_PATH_PROP to "\"${MinecraftNativesProvider.provideNativesFolder(project)}\""

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

        // Workaround old versions requiring classes that are not exported by default
        val isTaskToolchain9Plus =
            (this.javaLauncher.orNull?.metadata?.languageVersion
                ?: project.extensions.getByType(JavaPluginExtension::class.java).toolchain.languageVersion.orNull)?.canCompileOrRun(
                JavaLanguageVersion.of(9)
            )

        val isGradleVersion9Plus = javaVersion.isJava9Compatible
        val isJava9OrPlus = isTaskToolchain9Plus ?: isGradleVersion9Plus

        if (isJava9OrPlus) {
            jvmArgs = jvmArgs!! + listOf(
                // Add DNS module
                "--add-modules",
                "jdk.naming.dns",

                // Export DNS modules
                "--add-exports",
                "jdk.naming.dns/com.sun.jndi.dns=java.naming",

                // Open all classes just to be sure
                "--add-opens",
                "java.base/java.io=ALL-UNNAMED"
            )
        }
    }

    private fun getSpongeMixinJar(): File {
        val mixinsJarConfig = project.configurations.detachedConfiguration()
        mixinsJarConfig.dependencies.add(project.dependencies.create(mixinDependency))
        return mixinsJarConfig.resolve().first()
    }

    private fun setupGameLaunchArguments(extension: LightCraftGradleExtension) {
        val isOneDotSixOrHigher = extension.clientVersion.ordinal >= ClientVersion.V1_6_4.ordinal

        // Set up game launch arguments
        val launchArguments = mutableListOf<String>()

        // Tell orion-launcher that we are launching Mixins on the client side
        launchArguments += "--side"
        launchArguments += MIXIN_SIDE_CLIENT

        // Tell orion-launcher what class it is supposed to launch
        launchArguments += "--main-class"
        launchArguments += extension.clientVersion.mainClass

        (excludedPackages + extension.launchSettings.transformExcludedPackages).forEach { pkg ->
            launchArguments += "--excluded-packages"
            launchArguments += "\"$pkg\""
        }

        // Tell orion-launcher our mappings file
        if (extension.launchSettings.deobfuscateInDev) {
            launchArguments += "--mappings"
            launchArguments += MinecraftMappingsProvider.provideMappings(project).absolutePath
        }

        // Tell orion-launcher that we are passing-through the Minecraft arguments
        launchArguments += "--"

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

        // Provide our game assets index file
        if (isOneDotSixOrHigher) {
            launchArguments += "--assetIndex"
            launchArguments += "\"${MinecraftAssetsProvider.computeAssetIndexName(project)}\""
        }

        // Provide our game assets directory
        launchArguments += "--assetsDir"
        launchArguments += "\"${MinecraftAssetsProvider.provideMinecraftAssets(project).absolutePath}\""

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