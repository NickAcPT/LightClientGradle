package io.github.nickacpt.lightcraft.gradle.utils.fixes.guavafix

import io.github.nickacpt.lightcraft.gradle.ASM_VERSION
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GuavaIteratorsFixerClassVisitor(visitor: ClassVisitor?) : ClassVisitor(ASM_VERSION, visitor) {
    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        return GuavaIteratorsFixerMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions))
    }
}