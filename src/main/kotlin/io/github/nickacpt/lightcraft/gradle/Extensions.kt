package io.github.nickacpt.lightcraft.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.zeroturnaround.zip.ZipUtil
import java.io.File

const val loggerPrefix = ":LightCraft"

fun Project.evaluate(code: Project.() -> Unit) {
    if (project.state.executed) {
        project.code()
    } else {
        project.afterEvaluate(code)
    }
}

val Project.isOffline: Boolean
    get() = gradle.startParameter.isOffline

val Project.isRefreshDependencies: Boolean
    get() = gradle.startParameter.isRefreshDependencies

val Project.lightCraftExtension: LightCraftGradleExtension
    get() = extensions.getByType(LightCraftGradleExtension::class.java)


fun Project.getCacheFileDir(): File {
    return File(gradle.gradleUserHomeDir, "caches" + File.separatorChar + "light-craft").also { it.mkdirs() }
}

fun Project.getCachedFile(name: String, versioned: Boolean = true): File {
    val versionName = lightCraftExtension.clientVersion.friendlyName
    var parent = getCacheFileDir()
    if (versioned) parent = File(getCacheFileDir(), versionName).also { it.mkdirs() }

    return File(parent, name)
}

fun Project.getCachedFile(name: String, versioned: Boolean = true, fetchFile: (File) -> Unit): File {
    val finalFile = getCachedFile(name, versioned)
    getCachedFile(finalFile, fetchFile)

    return finalFile
}

fun Project.getCachedFile(finalFile: File, fetchFile: (File) -> Unit): File {
    val fileName = finalFile.name
    val wasDirectory = finalFile.isDirectory
    if (isRefreshDependencies || !finalFile.exists() || finalFile.length() == 0L) {
        if (finalFile.exists()) {
            if (wasDirectory) finalFile.deleteRecursively() else finalFile.delete()
        }
        if (isOffline) {
            logger.lifecycle("$loggerPrefix - Attempted to fetch file $fileName, but Gradle is in Offline mode")
            throw Exception("LightCraftGradle attempted to fetch $fileName, but it doesn't exist and Gradle is offline mode - Build cannot be finished")
        } else {
            if (wasDirectory) {
                finalFile.mkdirs()
            } else {
                finalFile.parentFile.mkdirs()
                finalFile.createNewFile()
            }
            fetchFile(finalFile)
        }
    }

    return finalFile
}

val Project.finalJarTask: Task
    get() = kotlin.runCatching { tasks.getByName("shadowJar") }.getOrNull() ?: tasks.getByName("jar")

fun removeSignature(jarFile: File) {
    ZipUtil.removeEntry(jarFile, "META-INF/MANIFEST.MF")
}