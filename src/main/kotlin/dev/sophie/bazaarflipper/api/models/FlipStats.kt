package dev.sophie.bazaarflipper.api.models

import java.time.Instant

data class FlipStats(
    var totalProfit: Double = 0.0,
    var flipCount: Int = 0,
    var lastFlippedItem: String = "Keins",
    var lastFlipProfit: Double = 0.0,
    var lastFlipTime: Instant = Instant.EPOCH
) {
    fun recordFlip(productId: String, profit: Double) {
        totalProfit += profit
        flipCount++
        lastFlippedItem = productId
        lastFlipProfit = profit
        lastFlipTime = Instant.now()
    }
    
    fun getTimeSinceLastFlip(): String {
        if (lastFlipTime == Instant.EPOCH) return "Nie"
        
        val seconds = java.time.Duration.between(lastFlipTime, Instant.now()).seconds
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
}
