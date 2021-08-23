package io.github.nickacpt.lightcraft.gradle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.objectweb.asm.Opcodes


const val JITPACK_LIBRARIES_BASE = "https://jitpack.io/"
const val FABRICMC_LIBRARIES_BASE = "https://maven.fabricmc.net/"
const val LIBRARIES_BASE = "https://libraries.minecraft.net/"
const val RESOURCES_BASE = "https://resources.download.minecraft.net/"
const val VERSION_MANIFESTS = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

const val MAPPING_SOURCE_NS = "official"
const val MAPPING_DEST_NS = "named"

const val LAUNCH_WRAPPER_CONFIGURATION = "launchWrapper"
const val JAR_MOD_CONFIGURATION = "minecraftJarMod"
const val MINECRAFT_LIBRARY_CONFIGURATION = "minecraftLibrary"

const val ASM_VERSION = Opcodes.ASM9

const val LIGHTCRAFT_TASK_GROUP = "lightcraft"

const val LAUNCHWRAPPER_GAME_CLASS_PROP = "lightcraft.launch.game"
const val LAUNCHWRAPPER_MAIN_CLASS_PROP = "lightcraft.launch.main"
const val LAUNCHWRAPPER_MIXIN_SIDE_PROP = "lightcraft.launch.mixin.side"

const val JVM_LIBRARY_PATH_PROP = "java.library.path"

const val MIXIN_SIDE_DEDICATEDSERVER = "DEDICATEDSERVER"
const val MIXIN_SIDE_SERVER = "SERVER"
const val MIXIN_SIDE_CLIENT = "CLIENT"
const val MIXIN_SIDE_UNKNOWN = "UNKNOWN"

const val LIGHTCRAFT_LAUNCH_PLAYER_NAME = "LightCraftDev"
const val LIGHTCRAFT_LAUNCH_DEV_ENV = "lightcraft.launch.dev"

private const val mixinsVersion = "0.9.2+mixin.0.8.2"
private const val asmVersion = "9.1"
private const val launchWrapperVersion = "21853d87de"

const val mixinDependency = "net.fabricmc:sponge-mixin:$mixinsVersion"
const val asmDependency = "org.ow2.asm:asm:$asmVersion"
const val asmTreeDependency = "org.ow2.asm:asm-tree:$asmVersion"
const val asmUtilDependency = "org.ow2.asm:asm-util:$asmVersion"
const val launchWrapperDependency = "com.github.NickAcPT:LegacyLauncher:$launchWrapperVersion"

val objectMapper by lazy { jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }