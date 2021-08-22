plugins {
    kotlin("jvm") version "1.5.10"
    java
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.nickacpt.lightclient"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleKotlinDsl())

    /* Mapping I/O */
    implementation("com.github.FabricMc:mapping-io:5879155d42")

    /* JSON I/O */
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")

    /* Jar Remapping */
    implementation("net.fabricmc:tiny-remapper:0.5.0")

    /* Jar Javadoc generation */
    implementation("net.fabricmc:mappingpoet:0.2.8")

    /* Zip I/O */
    implementation("org.zeroturnaround:zt-zip:1.14")

    /* Decompilers */
    implementation("net.fabricmc:fabric-fernflower:1.4.1")

    /* ASM */
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-analysis:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-util:9.2")

    /* Remapping helper - Decompilation */
    implementation("net.fabricmc:stitch:0.6.1") {
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
