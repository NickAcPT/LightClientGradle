package io.github.nickacpt.lightcraft.gradle.utils.fixes.guavafix

import io.github.nickacpt.lightcraft.gradle.ASM_VERSION
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class GuavaIteratorsFixerMethodVisitor(previousVisitor: MethodVisitor) : MethodVisitor(ASM_VERSION, previousVisitor) {
    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        var finalName = name
        var finalDescriptor = descriptor
        if (finalName == "emptyIterator" && owner == "com/google/common/collect/Iterators") {
            finalName = "forArray"
            finalDescriptor = finalDescriptor?.replace("()", "([Ljava/lang/Object;)")

            visitInsn(Opcodes.ICONST_0)
            visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")

        }
        super.visitMethodInsn(opcode, owner, finalName, finalDescriptor, isInterface)
    }
}
