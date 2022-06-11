package io.github.nickacpt.lightcraft.gradle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.objectweb.asm.Opcodes


const val FABRICMC_LIBRARIES_BASE = "https://maven.fabricmc.net/"
const val LIBRARIES_BASE = "https://libraries.minecraft.net/"
const val RESOURCES_BASE = "https://resources.download.minecraft.net/"
const val VERSION_MANIFESTS = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

const val MAPPING_SOURCE_NS = "official"
const val MAPPING_DEST_NS = "named"

const val ORION_LAUNCHER_CONFIGURATION = "orionLauncher"
const val JAR_MOD_CONFIGURATION = "minecraftJarMod"
const val MINECRAFT_LIBRARY_CONFIGURATION = "minecraftLibrary"
const val UPGRADED_MINECRAFT_LIBRARY_CONFIGURATION = "upgradedMinecraftLibrary"

const val ASM_VERSION = Opcodes.ASM9

const val LIGHTCRAFT_MINECRAFT_DEP_GROUP = "net.minecraft"
const val LIGHTCRAFT_MINECRAFT_DEP_NAME = "minecraft"

const val LIGHTCRAFT_TASK_GROUP = "lightcraft"

const val JVM_LIBRARY_PATH_PROP = "java.library.path"

const val MIXIN_SIDE_DEDICATEDSERVER = "DEDICATEDSERVER"
const val MIXIN_SIDE_SERVER = "SERVER"
const val MIXIN_SIDE_CLIENT = "CLIENT"
const val MIXIN_SIDE_UNKNOWN = "UNKNOWN"

const val LIGHTCRAFT_LAUNCH_PLAYER_NAME = "LightCraftDev"
const val LIGHTCRAFT_LAUNCH_DEV_ENV = "lightcraft.launch.dev"

const val MIXINS_DEBUG = "mixin.debug"

private const val mixinsVersion = "0.0.1+mixin.0.8.5"
private const val asmVersion = "9.3"
private const val orionLauncherVersion = "0.0.6-SNAPSHOT"

const val mixinDependency = "io.github.orioncraftmc:sponge-mixin:$mixinsVersion"
const val asmDependency = "org.ow2.asm:asm:$asmVersion"
const val asmTreeDependency = "org.ow2.asm:asm-tree:$asmVersion"
const val asmUtilDependency = "org.ow2.asm:asm-util:$asmVersion"
const val orionLauncherDependency = "io.github.orioncraftmc:orion-launcher:$orionLauncherVersion"

val objectMapper: ObjectMapper by lazy { jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }

val excludedPackages = listOf(
    "org.w3c.",
    "kotlin.",
    "kotlinx.",
)