package io.github.nickacpt.lightcraft.gradle.tasks

import io.github.nickacpt.lightcraft.gradle.providers.minecraft.MappedMinecraftProvider
import net.fabricmc.loom.decompilers.fernflower.FabricFernFlowerDecompiler
import net.fabricmc.loom.tasks.GenerateSourcesTask
import org.gradle.api.Project

object LightCraftGradleTasks {
    fun setupTasks(project: Project) {
        project.tasks.register("genSources", GenerateSourcesTask::class.java, FabricFernFlowerDecompiler(project)).get()
            .apply {
                inputJar = MappedMinecraftProvider.provideMappedMinecraftDependency(project)
            }
    }
}