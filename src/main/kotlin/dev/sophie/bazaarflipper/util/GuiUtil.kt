package dev.sophie.bazaarflipper.util

import dev.sophie.bazaarflipper.BazaarFlipperMod
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import java.util.regex.Pattern

object GuiUtil {
    private val PURSE_PATTERN = Pattern.compile("Purse: ([\\d,]+\\.?\\d*)")
    
    fun extractPurseFromScoreboard(client: MinecraftClient): Double? {
        val scoreboard = client.world?.scoreboard ?: return null
        val objective = scoreboard.getObjectiveForSlot(1) ?: return null
        
        for (score in scoreboard.getAllPlayerScores(objective)) {
            val playerName = score.playerName
            val matcher = PURSE_PATTERN.matcher(playerName)
            if (matcher.find()) {
                val purseStr = matcher.group(1).replace(",", "")
                return purseStr.toDoubleOrNull()
            }
        }
        
        return null
    }
    
    fun navigateToItem(client: MinecraftClient, screen: GenericContainerScreen, itemId: String): Boolean {
        // Durchsuche alle Slots in der GUI
        for (slot in screen.screenHandler.slots) {
            val stack = slot.stack
            if (isMatchingItem(stack, itemId)) {
                // Klicke auf den Slot
                HumanizationUtil.simulateClick(client, screen, slot)
                return true
            }
        }
        
        // Item nicht gefunden, versuche in Kategorien zu navigieren
        // Dies würde eine tiefere Implementierung erfordern, um Kategorien zu erkennen
        
        return false
    }
    
    fun placeBuyOrder(client: MinecraftClient, screen: HandledScreen<*>, price: Double, amount: Int): Boolean {
        try {
            // In einer realen Implementierung würden wir:
            // 1. Den "Instant Buy"-Button finden und klicken
            // 2. Das Preisfeld finden und den Preis eingeben
            // 3. Das Mengenfeld finden und die Menge eingeben
            // 4. Den "Bestätigen"-Button finden und klicken
            
            // Simuliere erfolgreiche Platzierung für jetzt
            return true
        } catch (e: Exception) {
            BazaarFlipperMod.LOGGER.error("Fehler beim Platzieren einer Kauforder", e)
            return false
        }
    }
    
    fun placeSellOrder(client: MinecraftClient, screen: HandledScreen<*>, price: Double, amount: Int): Boolean {
        try {
            // Ähnlich wie placeBuyOrder, aber für Verkaufsaufträge
            return true
        } catch (e: Exception) {
            BazaarFlipperMod.LOGGER.error("Fehler beim Platzieren einer Verkaufsorder", e)
            return false
        }
    }
    
    private fun isMatchingItem(stack: ItemStack, itemId: String): Boolean {
        // Prüfe, ob der Item-Stack dem gesuchten Bazaar-Item entspricht
        if (stack.isEmpty) return false
        
        val nbt = stack.nbt
        if (nbt != null && nbt.contains("ExtraAttributes")) {
            val extraAttr = nbt.getCompound("ExtraAttributes")
            if (extraAttr.contains("id")) {
                val id = extraAttr.getString("id")
                return id.equals(itemId, ignoreCase = true)
            }
        }
        
        // Fallback: Prüfe den Anzeigenamen
        val displayName = stack.name.string
        val normalizedItemId = itemId.replace("_", " ")
        return displayName.contains(normalizedItemId, ignoreCase = true)
    }
}
