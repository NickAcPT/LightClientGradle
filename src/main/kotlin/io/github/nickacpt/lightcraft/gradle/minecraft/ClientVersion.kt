package io.github.nickacpt.lightcraft.gradle.minecraft

enum class ClientVersion(
    val friendlyName: String,
    val mainClass: String,
    val gameClass: String = mainClass,
    val hasLegacyAssets: Boolean = false,
    val hasExtraMappings: Boolean = false
) {
    V1_5_2("1.5.2", "net.minecraft.client.Minecraft", hasLegacyAssets = true, hasExtraMappings = true),
    V1_8_9("1.8.9", "net.minecraft.client.main.Main", "net.minecraft.client.Minecraft")
}