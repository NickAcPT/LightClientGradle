package io.github.nickacpt.lightcraft.gradle

open class LightCraftLaunchSettings {
    var playerName = LIGHTCRAFT_LAUNCH_PLAYER_NAME

    var enableMixinsDebug = false

    var deobfuscateInDev = false

    var transformExcludedPackages = mutableListOf<String>()
}