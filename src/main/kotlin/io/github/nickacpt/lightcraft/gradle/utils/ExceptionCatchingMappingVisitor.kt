package io.github.nickacpt.lightcraft.gradle.utils

import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor

class ExceptionCatchingMappingVisitor(next: MappingVisitor) : ForwardingMappingVisitor(next) {
    override fun visitDstName(targetKind: MappedElementKind?, namespace: Int, name: String?) {
        kotlin.runCatching { super.visitDstName(targetKind, namespace, name) }
    }
}