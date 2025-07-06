plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("fabric-loom") version "1.2.+"  // Verwende eine stabilere Version mit Versionsbereich
    id("maven-publish")
}

group = "dev.sophie"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://server.bbkr.space/artifactory/libs-release") } // Fabric Loom repository
}

dependencies {
    // Minecraft & Fabric
    minecraft("com.mojang:minecraft:1.21.5")
    mappings("net.fabricmc:yarn:1.21.5+build.1")
    modImplementation("net.fabricmc:fabric-loader:0.15.0")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.91.1+1.21.5")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.0+kotlin.1.9.0")
    
    // Kotlin serialization für JSON-Verarbeitung
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Coroutines für asynchrones Arbeiten
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }
    
    jar {
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }
    
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
