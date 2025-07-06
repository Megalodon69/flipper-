package dev.sophie.bazaarflipper.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sophie.bazaarflipper.BazaarFlipperMod
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class ModConfig(
    var apiKey: String = "",
    var hudX: Int = 5,
    var hudY: Int = 5,
    var minProfitPercentage: Double = 3.5,
    var maxBudgetPercentage: Double = 20.0,
    var minDelay: Long = 100,
    var maxDelay: Long = 400
) {
    companion object {
        private val configFile = File(
            FabricLoader.getInstance().configDir.toFile(),
            "${BazaarFlipperMod.MOD_ID}.json"
        )
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        fun load(): ModConfig {
            return try {
                if (configFile.exists()) {
                    FileReader(configFile).use { 
                        gson.fromJson(it, ModConfig::class.java)
                    }
                } else {
                    val config = ModConfig()
                    save(config)
                    config
                }
            } catch (e: Exception) {
                BazaarFlipperMod.LOGGER.error("Fehler beim Laden der Konfiguration", e)
                ModConfig()
            }
        }

        fun save(config: ModConfig) {
            try {
                configFile.parentFile.mkdirs()
                FileWriter(configFile).use { gson.toJson(config, it) }
            } catch (e: Exception) {
                BazaarFlipperMod.LOGGER.error("Fehler beim Speichern der Konfiguration", e)
            }
        }
    }
}
