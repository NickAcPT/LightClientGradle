package io.github.nickacpt.lightcraft.gradle.utils

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import java.io.File


fun Project.getMixinFiles(): List<File> {
    val sourceSets = extensions.getByType<JavaPluginExtension>().sourceSets
    val mixinFiles = sourceSets["main"]?.resources?.srcDirs?.first()?.listFiles()
        ?.filter { it.nameWithoutExtension.startsWith("mixins.") && it.name.endsWith(".json") }
    return mixinFiles ?: emptyList()
}