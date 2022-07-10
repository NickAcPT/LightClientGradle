package io.github.nickacpt.lightcraft.gradle.utils

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.extension
import kotlin.io.path.readBytes

object AsmUtils {

    fun viewJarAsNodes(jar: Path): Map<String, ClassNode> {
        val nodes = mutableMapOf<String, ClassNode>()
        FileSystems.newFileSystem(jar).use { fs ->
            Files.walkFileTree(fs.getPath("/"), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    if (file.extension == "class") {
                        val reader = ClassReader(file.readBytes())
                        val node = ClassNode()

                        reader.accept(node, 0)

                        nodes[node.name] = node

                    }
                    return FileVisitResult.CONTINUE
                }
            })
        }

        return nodes
    }

}