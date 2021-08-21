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
