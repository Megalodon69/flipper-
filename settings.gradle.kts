pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.quiltmc.org/repository/release/") } // Quilt repo enthält auch einige Fabric-Ressourcen
        maven { url = uri("https://maven.minecraftforge.net") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "bazaarflipper"
