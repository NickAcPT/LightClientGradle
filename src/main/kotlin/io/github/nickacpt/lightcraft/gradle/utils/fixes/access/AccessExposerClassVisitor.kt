package io.github.nickacpt.lightcraft.gradle.utils.fixes.access

import io.github.nickacpt.lightcraft.gradle.ASM_VERSION
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AccessExposerClassVisitor(writer: ClassVisitor? = null) : ClassVisitor(ASM_VERSION, writer) {
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        val accessVar =
            access and (Opcodes.ACC_PRIVATE or Opcodes.ACC_PROTECTED).inv() or Opcodes.ACC_PUBLIC
        super.visit(version, accessVar, name, signature, superName, interfaces)
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        val accessVar =
            access and (Opcodes.ACC_PRIVATE or Opcodes.ACC_PROTECTED).inv() or Opcodes.ACC_PUBLIC
        return super.visitField(accessVar, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor? {
        val accessVar =
            access and (Opcodes.ACC_PRIVATE or Opcodes.ACC_PROTECTED).inv() or Opcodes.ACC_PUBLIC
        return super.visitMethod(accessVar, name, descriptor, signature, exceptions)
    }
}