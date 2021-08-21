package io.github.nickacpt.lightcraft.gradle.utils

import org.zeroturnaround.zip.ByteSource
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun mergeZip(
    input: File,
    output: File,
    classPrefix: String = "",
    condition: (ZipEntry) -> Boolean = { true }
) {
    mergeZip(input, output, classPrefix, condition) { _, bytes -> bytes }
}

fun mergeZip(
    input: File,
    output: File,
    classPrefix: String = "",
    condition: (ZipEntry) -> Boolean = { true },
    byteMapper: (ZipEntry, ByteArray) -> ByteArray
) {
    ZipFile(input).use { zip ->
        val newEntries = zip.entries().iterator().asSequence().mapNotNull { zipEntry ->
            val prefix = if (zipEntry.name.endsWith(".class")) classPrefix else ""
            if (!condition(zipEntry) || zipEntry.isDirectory) return@mapNotNull null
            ByteSource(
                prefix + zipEntry.name,
                zip.getInputStream(zipEntry).use { byteMapper(zipEntry, it.readAllBytes()) },
                zipEntry.time
            )
        }.toList()

        ZipUtil.addOrReplaceEntries(output, *newEntries.toTypedArray())
    }
}