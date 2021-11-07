package io.github.nickacpt.lightcraft.gradle.minecraft

enum class ClientVersion(
    val friendlyName: String,
    val mainClass: String,
    val gameClass: String = mainClass,
    val hasLegacyAssets: Boolean = false,
    val hasExtraMappings: Boolean = false,
    val hasMojangMappings: Boolean = false,
    val shipsLog4J: Boolean = true
) {
    V1_5_2("1.5.2", "net.minecraft.client.Minecraft", hasLegacyAssets = true, hasExtraMappings = true, shipsLog4J = false),
    V1_6_4("1.6.4", "net.minecraft.client.main.Main", "net.minecraft.client.Minecraft", shipsLog4J = false),
    V1_7_10("1.7.10", "net.minecraft.client.main.Main", "net.minecraft.client.Minecraft"),
    V1_8_9("1.8.9", "net.minecraft.client.main.Main", "net.minecraft.client.Minecraft"),

    V1_17_1("1.17.1", "net.minecraft.client.main.Main", "net.minecraft.client.Minecraft", hasMojangMappings = true)
}