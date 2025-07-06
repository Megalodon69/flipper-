package dev.sophie.bazaarflipper.api

import dev.sophie.bazaarflipper.BazaarFlipperMod
import dev.sophie.bazaarflipper.api.models.BazaarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference

class HypixelApiClient(private var apiKey: String) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cachedData = AtomicReference<BazaarData?>(null)
    private var lastRequestTime: Instant = Instant.EPOCH
    
    suspend fun getBazaarData(forceFresh: Boolean = false): Result<BazaarData> {
        val cached = cachedData.get()
        val now = Instant.now()
        
        // Cache verwenden, wenn verfügbar und weniger als 30 Sekunden alt
        if (!forceFresh && cached != null && 
            ChronoUnit.SECONDS.between(lastRequestTime, now) < 30) {
            return Result.success(cached)
        }
        
        return try {
            // Rate-Limit einhalten: mindestens 2 Sekunden zwischen Anfragen
            val timeSinceLastRequest = ChronoUnit.MILLIS.between(lastRequestTime, now)
            if (timeSinceLastRequest < 2000) {
                withContext(Dispatchers.IO) {
                    Thread.sleep(2000 - timeSinceLastRequest)
                }
            }
            
            val data = withContext(Dispatchers.IO) {
                fetchDataFromApi()
            }
            cachedData.set(data)
            lastRequestTime = Instant.now()
            Result.success(data)
        } catch (e: Exception) {
            BazaarFlipperMod.LOGGER.error("Fehler beim Abrufen der Bazaar-Daten", e)
            Result.failure(e)
        }
    }
    
    fun updateApiKey(newKey: String) {
        apiKey = newKey
        BazaarFlipperMod.LOGGER.info("API-Schlüssel aktualisiert")
    }
    
    private fun fetchDataFromApi(): BazaarData {
        val connection = URL("https://api.hypixel.net/skyblock/bazaar").openConnection() as HttpURLConnection
        connection.setRequestProperty("API-Key", apiKey)
        connection.requestMethod = "GET"
        
        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            return json.decodeFromString(BazaarData.serializer(), response)
        } finally {
            connection.disconnect()
        }
    }
}
