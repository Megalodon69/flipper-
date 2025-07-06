package dev.sophie.bazaarflipper.flipping

import dev.sophie.bazaarflipper.BazaarFlipperMod
import dev.sophie.bazaarflipper.api.models.BazaarData
import dev.sophie.bazaarflipper.api.models.FlipCandidate
import dev.sophie.bazaarflipper.api.models.FlipStats
import dev.sophie.bazaarflipper.util.GuiUtil
import dev.sophie.bazaarflipper.util.HumanizationUtil
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.text.Text
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class FlippingManager {
    val stats = FlipStats()
    private val currentBazaarData = AtomicReference<BazaarData?>(null)
    private var currentState = FlipState.IDLE
    private var currentFlipCandidate: FlipCandidate? = null
    private var stateEnteredTime = Instant.now()
    private var currentPurse = 0.0
    
    private var tickCounter = 0
    
    enum class FlipState {
        IDLE, FETCHING_DATA, OPENING_BAZAAR, NAVIGATING_TO_ITEM, 
        PLACING_BUY_ORDER, WAITING_FOR_FILL, PLACING_SELL_ORDER, COOLDOWN
    }
    
    fun tick(client: MinecraftClient) {
        if (client.player == null) return
        
        tickCounter++
        if (tickCounter % 20 != 0) return // Nur alle 20 Ticks ausführen (ca. 1 Sekunde)
        
        when (currentState) {
            FlipState.IDLE -> {
                currentState = FlipState.FETCHING_DATA
                stateEnteredTime = Instant.now()
                BazaarFlipperMod.MOD_SCOPE.launch {
                    updateBazaarData()
                }
            }
            
            FlipState.FETCHING_DATA -> {
                val data = currentBazaarData.get()
                if (data != null) {
                    // Extrahiere den aktuellen Geldbeutel (purse) aus dem Scoreboard
                    currentPurse = GuiUtil.extractPurseFromScoreboard(client) ?: 0.0
                    
                    // Finde den profitabelsten Gegenstand zum Flippen
                    currentFlipCandidate = findBestFlip(data)
                    
                    if (currentFlipCandidate != null) {
                        currentState = FlipState.OPENING_BAZAAR
                        stateEnteredTime = Instant.now()
                    } else {
                        // Keine profitablen Items gefunden
                        client.player?.sendMessage(Text.literal("§cKeine profitablen Gegenstände gefunden. Versuche es später erneut."))
                        cooldownAndRetry()
                    }
                } else if (Instant.now().isAfter(stateEnteredTime.plusSeconds(10))) {
                    // Timeout nach 10 Sekunden
                    client.player?.sendMessage(Text.literal("§cZeitüberschreitung beim Abrufen der Bazaar-Daten."))
                    cooldownAndRetry()
                }
            }
            
            FlipState.OPENING_BAZAAR -> {
                // Sende den Befehl, um den Basar zu öffnen
                client.player?.sendCommand("bz")
                currentState = FlipState.NAVIGATING_TO_ITEM
                stateEnteredTime = Instant.now()
                HumanizationUtil.scheduleDelayedAction(client) {
                    // Warte auf die GUI
                }
            }
            
            FlipState.NAVIGATING_TO_ITEM -> {
                val screen = client.currentScreen
                if (screen is GenericContainerScreen) {
                    val item = currentFlipCandidate ?: return
                    
                    // Navigiere zum Item im Basar-GUI
                    if (GuiUtil.navigateToItem(client, screen, item.productId)) {
                        currentState = FlipState.PLACING_BUY_ORDER
                        stateEnteredTime = Instant.now()
                    } else if (Instant.now().isAfter(stateEnteredTime.plusSeconds(5))) {
                        // Timeout nach 5 Sekunden
                        client.player?.sendMessage(Text.literal("§cKann Gegenstand im Basar nicht finden."))
                        cooldownAndRetry()
                    }
                } else if (Instant.now().isAfter(stateEnteredTime.plusSeconds(3))) {
                    // Timeout nach 3 Sekunden
                    currentState = FlipState.OPENING_BAZAAR
                    stateEnteredTime = Instant.now()
                }
            }
            
            FlipState.PLACING_BUY_ORDER -> {
                // Kaufauftrag platzieren
                val item = currentFlipCandidate ?: return
                val screen = client.currentScreen
                
                if (screen is GenericContainerScreen) {
                    val budget = currentPurse * (BazaarFlipperMod.CONFIG.maxBudgetPercentage / 100.0)
                    val amountToBuy = (budget / item.buyPrice).toInt()
                    
                    if (GuiUtil.placeBuyOrder(client, screen, item.buyPrice, amountToBuy)) {
                        currentState = FlipState.WAITING_FOR_FILL
                        stateEnteredTime = Instant.now()
                    }
                } else {
                    // GUI wurde geschlossen
                    cooldownAndRetry()
                }
            }
            
            FlipState.WAITING_FOR_FILL -> {
                // In einer echten Implementierung würden wir den Status des Auftrags überprüfen
                // Für Demo-Zwecke simulieren wir eine verzögerte Erfüllung
                if (Instant.now().isAfter(stateEnteredTime.plusSeconds(3))) {
                    currentState = FlipState.PLACING_SELL_ORDER
                    stateEnteredTime = Instant.now()
                }
            }
            
            FlipState.PLACING_SELL_ORDER -> {
                // Verkaufsauftrag platzieren
                val item = currentFlipCandidate ?: return
                val screen = client.currentScreen
                
                if (screen is GenericContainerScreen) {
                    val budget = currentPurse * (BazaarFlipperMod.CONFIG.maxBudgetPercentage / 100.0)
                    val amountSold = (budget / item.buyPrice).toInt()
                    
                    if (GuiUtil.placeSellOrder(client, screen, item.sellPrice, amountSold)) {
                        // Erfolgreicher Flip
                        val profit = calculateProfit(item)
                        stats.recordFlip(item.productId, profit)
                        
                        client.player?.sendMessage(Text.literal("§aErfolgreich geflippt: §f${item.productId} §aGewinn: §f$profit Münzen"))
                        
                        cooldownAndRetry()
                    }
                } else {
                    cooldownAndRetry()
                }
            }
            
            FlipState.COOLDOWN -> {
                // Warte zwischen den Flips
                if (Instant.now().isAfter(stateEnteredTime.plusSeconds(5))) {
                    currentState = FlipState.IDLE
                }
            }
        }
    }
    
    private suspend fun updateBazaarData() {
        val result = BazaarFlipperMod.API_CLIENT.getBazaarData(true)
        result.fold(
            onSuccess = { 
                currentBazaarData.set(it)
            },
            onFailure = {
                BazaarFlipperMod.LOGGER.error("Fehler beim Aktualisieren der Bazaar-Daten", it)
            }
        )
    }
    
    private fun findBestFlip(data: BazaarData): FlipCandidate? {
        val minProfitPercentage = BazaarFlipperMod.CONFIG.minProfitPercentage
        
        return data.products.values
            .asSequence()
            .filter { it.quick_status.buyPrice > 0 && it.quick_status.sellPrice > 0 }
            .map {
                val buyPrice = it.quick_status.buyPrice * 1.01 // Inklusive Gebühr
                val sellPrice = it.quick_status.sellPrice * 0.99 // Inklusive Gebühr
                val profit = sellPrice - buyPrice
                val profitPercentage = (profit / buyPrice) * 100.0
                
                FlipCandidate(
                    productId = it.product_id,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    profit = profit,
                    profitPercentage = profitPercentage,
                    volume = minOf(it.quick_status.buyMovingWeek, it.quick_status.sellMovingWeek) / 7
                )
            }
            .filter { it.profitPercentage >= minProfitPercentage && it.volume > 1000 }
            .sortedByDescending { it.profit * it.volume }
            .firstOrNull()
    }
    
    private fun calculateProfit(item: FlipCandidate): Double {
        val budget = currentPurse * (BazaarFlipperMod.CONFIG.maxBudgetPercentage / 100.0)
        val amountBought = (budget / item.buyPrice).toInt()
        
        return (item.sellPrice - item.buyPrice) * amountBought
    }
    
    private fun cooldownAndRetry() {
        currentState = FlipState.COOLDOWN
        stateEnteredTime = Instant.now()
        currentFlipCandidate = null
    }
}
