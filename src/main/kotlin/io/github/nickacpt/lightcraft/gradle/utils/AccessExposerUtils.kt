package io.github.nickacpt.lightcraft.gradle.utils

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

internal fun File.exposeAccessToPublic() {
    FileSystems.newFileSystem(toPath()).use { inputFs ->
        Files.walkFileTree(inputFs.getPath("/"), object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                if (file.fileName.toString().endsWith(".class")) {
                    val bytes = Files.readAllBytes(file)
                    val reader = ClassReader(bytes)
                    val writer = ClassWriter(0)

                    reader.accept(AccessExposerClassVisitor(writer), 0)

                    Files.deleteIfExists(file)
                    Files.write(
                        file,
                        writer.toByteArray()
                    )

                }
                return FileVisitResult.CONTINUE
            }
        })

    }
}
