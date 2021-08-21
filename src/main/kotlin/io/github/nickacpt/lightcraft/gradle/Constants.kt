package io.github.nickacpt.lightcraft.gradle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

const val FABRICMC_LIBRARIES_BASE = "https://maven.fabricmc.net/"
const val LIBRARIES_BASE = "https://libraries.minecraft.net/"
const val RESOURCES_BASE = "https://resources.download.minecraft.net/"
const val VERSION_MANIFESTS = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

const val MAPPING_SOURCE_NS = "official"
const val MAPPING_DEST_NS = "named"

const val JAR_MOD_CONFIGURATION = "minecraftJarMod"
const val MINECRAFT_LIBRARY_CONFIGURATION = "minecraftLibrary"

val objectMapper by lazy { jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }