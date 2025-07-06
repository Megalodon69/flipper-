package dev.sophie.bazaarflipper.util

import dev.sophie.bazaarflipper.BazaarFlipperMod
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.screen.slot.Slot
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.SlotActionType

object HumanizationUtil {
    fun getRandomDelay(): Long {
        val config = BazaarFlipperMod.CONFIG
        return Random.nextLong(config.minDelay, config.maxDelay)
    }
    
    fun scheduleDelayedAction(client: MinecraftClient, action: () -> Unit) {
        val delay = getRandomDelay()
        BazaarFlipperMod.MOD_SCOPE.launch {
            delay(delay)
            client.execute(action)
        }
    }
    
    fun simulateClick(client: MinecraftClient, screen: HandledScreen<*>, slot: Slot) {
        val randomOffsetX = Random.nextDouble(-8.0, 8.0)
        val randomOffsetY = Random.nextDouble(-8.0, 8.0)
        
        // Position des Slots in der GUI berechnen
        val x = slot.x + 8 + randomOffsetX.toInt()
        val y = slot.y + 8 + randomOffsetY.toInt()
        
        scheduleDelayedAction(client) {
            // Mausklick auf den Slot simulieren
            screen.onMouseClick(slot, slot.id, 0, SlotActionType.PICKUP)
        }
    }
    
    fun simulateSmoothMouseMovement(client: MinecraftClient, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        BazaarFlipperMod.MOD_SCOPE.launch {
            // Berechne Zwischenpunkte für flüssige Bewegung
            val steps = 10 + Random.nextInt(5)
            for (i in 0..steps) {
                val progress = i.toDouble() / steps
                
                // Leichte Abweichung für natürlichere Bewegung
                val deviation = if (i > 0 && i < steps) Random.nextDouble(-2.0, 2.0) else 0.0
                
                val currentX = fromX + (toX - fromX) * progress + deviation
                val currentY = fromY + (toY - fromY) * progress + deviation
                
                // In einer echten Implementierung würden wir die Maus tatsächlich bewegen
                
                delay(8 + Random.nextLong(5))
            }
        }
    }
    
    fun simulateTyping(client: MinecraftClient, text: String) {
        BazaarFlipperMod.MOD_SCOPE.launch {
            for (c in text) {
                val delay = Random.nextLong(50, 150)
                delay(delay)
                
                client.execute {
                    // In einer echten Implementierung würden wir Tastenanschläge simulieren
                }
            }
        }
    }
}
package dev.sophie.bazaarflipper.util

import dev.sophie.bazaarflipper.BazaarFlipperMod
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.screen.slot.Slot
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW

object HumanizationUtil {
    fun getRandomDelay(): Long {
        val config = BazaarFlipperMod.CONFIG
        return Random.nextLong(config.minDelay, config.maxDelay)
    }
    
    fun scheduleDelayedAction(client: MinecraftClient, action: () -> Unit) {
        val delay = getRandomDelay()
        BazaarFlipperMod.MOD_SCOPE.launch {
            delay(delay)
            client.execute(action)
        }
    }
    
    fun simulateClick(client: MinecraftClient, screen: HandledScreen<*>, slot: Slot) {
        val randomOffsetX = Random.nextDouble(-8.0, 8.0)
        val randomOffsetY = Random.nextDouble(-8.0, 8.0)
        
        // Berechne die Position des Slots in der GUI
        val x = slot.x + 8 + randomOffsetX.toInt()
        val y = slot.y + 8 + randomOffsetY.toInt()
        
        // In einer echten Implementierung würden wir die Maus wirklich bewegen
        // und dann den Klick simulieren
        
        scheduleDelayedAction(client) {
            // Simuliere Mausklick auf den Slot
            screen.onMouseClick(slot, slot.id, 0, SlotActionType.PICKUP)
        }
    }
    
    fun simulateTyping(client: MinecraftClient, text: String) {
        BazaarFlipperMod.MOD_SCOPE.launch {
            for (c in text) {
                val delay = Random.nextLong(50, 150)
                delay(delay)
                
                client.execute {
                    // In einer echten Implementierung würden wir Tastenanschläge simulieren
                    // über GLFW oder ähnliches
                }
            }
        }
    }
}
