package io.github.nickacpt.lightcraft.gradle.utils

import io.github.nickacpt.lightcraft.gradle.minecraft.ClientVersion
import io.github.nickacpt.lightcraft.gradle.utils.fixes.access.AccessExposerClassVisitor
import io.github.nickacpt.lightcraft.gradle.utils.fixes.guavafix.GuavaIteratorsFixerClassVisitor
import org.mcphackers.rdi.injector.RDInjector
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

internal fun File.peformMiscAsmProcessing(version: ClientVersion, fixParameters: Boolean = false) {

    val nodes = AsmUtils.viewJarAsNodes(toPath())

    val newNodes = mutableListOf<ClassNode>()

    nodes.forEach { (_, node) ->
        val newNode = ClassNode()
        node.accept(GuavaIteratorsFixerClassVisitor(AccessExposerClassVisitor(newNode)))

        newNodes.add(newNode)
    }

    var rdInjector: RDInjector? = null

    if (version.requiresRetroDebug) {
        rdInjector = RDInjector(newNodes)
            .fixInnerClasses()
            .fixAccess()
            .also { if (fixParameters) it.fixParameterLVT() }
            .fixImplicitConstructors()
            .guessAnonymousInnerClasses()

        rdInjector.transform()
    }

    FileSystems.newFileSystem(toPath()).use { inputFs ->
        Files.walkFileTree(inputFs.getPath("/"), object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                if (file.fileName.toString().endsWith(".class")) {
                    val removeSuffix = file.fileName.toString().removeSuffix(".class")

                    val writer = ClassWriter(0)
                    val node = rdInjector?.storage?.getClass(removeSuffix) ?: ClassNode().also {
                        ClassReader(Files.readAllBytes(file)).accept(it, 0)
                    }

                    node.accept(writer)

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
