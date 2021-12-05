plugins {
    kotlin("jvm") version "1.5.32"
    java
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.nickacpt.lightclient"
version = "1.3.2-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleKotlinDsl())

    /* Mapping I/O */
    implementation("com.github.FabricMc:mapping-io:5879155d42")

    /* JSON I/O */
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")

    /* Jar Remapping */
    implementation("org.quiltmc:tiny-remapper:0.4.3")

    /* Zip I/O */
    implementation("org.zeroturnaround:zt-zip:1.14")

    /* Decompiler */
    implementation("org.quiltmc:quiltflower:1.5.0")

    /* ASM */
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-analysis:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-util:9.2")

    /* Remapping helper - Decompilation */
    implementation("org.quiltmc:stitch:0.6.2") {
        exclude(module = "enigma")
    }
}

gradlePlugin {
    plugins {
        create("LightCraftGradle") {
            id = "io.github.nickacpt.lightcraft.gradle"
            implementationClass = "io.github.nickacpt.lightcraft.gradle.LightCraftGradlePlugin"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "16"
        sourceCompatibility = "16"
        targetCompatibility = "16"
    }
}


publishing {
    repositories {
        val lightCraftRepoDir = project.findProperty("lightcraft.repo.location")
        if (lightCraftRepoDir != null) {
            maven {
                name = "LightCraftRepo"
                url = File(lightCraftRepoDir.toString()).toURI()
            }
        }
    }
}
