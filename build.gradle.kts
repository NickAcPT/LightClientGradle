plugins {
    kotlin("jvm") version "1.5.32"
    java
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.nickacpt.lightclient"
version = "2.1.4-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://raw.githubusercontent.com/OrionCraftMC/OrionMaven/main/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleKotlinDsl())

    /* Mapping I/O */
    implementation("com.github.FabricMc:mapping-io:597f0722d6")

    /* JSON I/O */
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

    /* Jar Remapping */
    implementation("net.fabricmc:tiny-remapper:0.8.5")

    /* Zip I/O */
    implementation("org.zeroturnaround:zt-zip:1.15")

    /* Decompiler */
    implementation("org.quiltmc:quiltflower:1.8.0")

    /* ASM */
    implementation("org.ow2.asm:asm:9.3")
    implementation("org.ow2.asm:asm-analysis:9.3")
    implementation("org.ow2.asm:asm-commons:9.3")
    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("org.ow2.asm:asm-util:9.3")

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

val targetJavaVersion = 16

tasks {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = targetJavaVersion.toString()
        sourceCompatibility = targetJavaVersion.toString()
        targetCompatibility = targetJavaVersion.toString()
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
