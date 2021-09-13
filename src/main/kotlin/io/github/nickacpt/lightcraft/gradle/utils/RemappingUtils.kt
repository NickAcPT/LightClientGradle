package io.github.nickacpt.lightcraft.gradle.utils

import net.fabricmc.loom.util.TinyRemapperMappingsHelper
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.fabricmc.tinyremapper.NonClassCopyMode
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.nio.file.Path

fun Project.resolveClasspathAsPath(): List<Path> {
    return project.configurations.filter { it.isCanBeResolved }
        .flatMap { conf -> conf.resolve().map { it.toPath() } }
}


data class RemapMappingFile(val file: File, val from: String, val to: String) {
    fun reverse(): RemapMappingFile {
        return this.copy(from = to, to = from)
    }
}

fun remapJar(
    project: Project,
    inputFile: File,
    output: File,
    mappings: RemapMappingFile?,
    resolveClassPath: Boolean = false
) {
    val remapper = TinyRemapper.newRemapper()
        .also { builder ->
            if (mappings == null) return@also

            val tree = MemoryMappingTree()
            mappings.file.reader().use {
                MappingReader.read(it, tree)
            }

            builder.withMappings(
                TinyRemapperMappingsHelper.create(
                    tree,
                    mappings.from,
                    mappings.to,
                    true
                )
            )
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .fixPackageAccess(true)
                .ignoreConflicts(true)
        }.build()

    try {
        val input = inputFile.toPath()

        OutputConsumerPath.Builder(output.toPath()).build().use { outputConsumer ->
            outputConsumer.addNonClassFiles(input, NonClassCopyMode.SKIP_META_INF, remapper)
            remapper.readInputs(input)
            if (resolveClassPath)
                remapper.readClassPath(*project.resolveClasspathAsPath().toTypedArray())
            remapper.apply(outputConsumer)
        }
    } catch (e: IOException) {
        throw RuntimeException(e)
    } finally {
        remapper.finish()
    }
}