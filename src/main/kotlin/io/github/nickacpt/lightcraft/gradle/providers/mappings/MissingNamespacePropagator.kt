package io.github.nickacpt.lightcraft.gradle.providers.mappings

import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MappingTree.ElementMapping

class MissingNamespacePropagator(
    private val tree: MappingTree,
    next: MappingVisitor
) : ForwardingMappingVisitor(next) {
    var namespaces = mutableListOf<String>()

    override fun visitNamespaces(srcNamespace: String, dstNamespaces: MutableList<String>) {
        namespaces = dstNamespaces
        super.visitNamespaces(srcNamespace, dstNamespaces)
    }

    private var clazz: MappingTree.ClassMapping? = null
    override fun visitClass(srcName: String?): Boolean {
        clazz = tree.getClass(srcName) ?: return super.visitClass(srcName)
        propagateNamesToMissingNamespaces(clazz)
        return super.visitClass(srcName)
    }

    private fun propagateNamesToMissingNamespaces(member: ElementMapping?) {
        var lastName: String? = null
        namespaces.forEach { ns ->
            val nsId = tree.getNamespaceId(ns)
            val currentName = member?.getDstName(nsId)
            if (currentName != null) {
                lastName = currentName
            } else if (lastName != null) {
                member?.setDstName(lastName, nsId)
            }
        }
    }

    override fun visitField(srcName: String?, srcDesc: String?): Boolean {
        if (srcName != null && clazz != null) {
            val field = clazz!!.getField(srcName, srcDesc)
            propagateNamesToMissingNamespaces(field)
        }
        return super.visitField(srcName, srcDesc)
    }

    override fun visitMethod(srcName: String?, srcDesc: String?): Boolean {
        if (srcName != null && clazz != null) {
            val method = clazz!!.getMethod(srcName, srcDesc)
            propagateNamesToMissingNamespaces(method)
        }
        return super.visitMethod(srcName, srcDesc)
    }
}