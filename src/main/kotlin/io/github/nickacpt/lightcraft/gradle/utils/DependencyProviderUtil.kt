package io.github.nickacpt.lightcraft.gradle.utils
import io.github.nickacpt.lightcraft.gradle.getCachedFile
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.create
import java.io.File

fun Project.provideDependency(group: String, name: String, version: String, configuration: String = JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, file: File): File {
    val providedDependency = project.dependencies.create(
        group,
        name,
        version
    )

    val dependencyFolderName = "dependency"
    project.getCachedFile(dependencyFolderName).mkdirs()
    val finalJarFile = project.getCachedFile(dependencyFolderName + File.separatorChar + "$name-$version.jar") {
        file.copyTo(it.also { it.delete() }, true)
    }

    project.repositories.flatDir { it.dir(finalJarFile.parentFile) }

    project.dependencies.add(configuration, providedDependency)

    return finalJarFile
}