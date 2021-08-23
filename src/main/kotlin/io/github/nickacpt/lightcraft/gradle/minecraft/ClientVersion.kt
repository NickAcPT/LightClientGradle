package io.github.nickacpt.lightcraft.gradle.minecraft

enum class ClientVersion(val friendlyName: String, val mainClass: String, val hasLegacyAssets: Boolean = false) {
    V1_5_2("1.5.2", "net.minecraft.client.Minecraft", hasLegacyAssets = true)
}