package dev.sophie.bazaarflipper.gui

import dev.sophie.bazaarflipper.BazaarFlipperMod
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.text.DecimalFormat

class HudOverlay : HudRenderCallback {
    private var isDragging = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private val formatter = DecimalFormat("#,###.##")
    
    override fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        if (!BazaarFlipperMod.showHud) return
        
        val client = MinecraftClient.getInstance()
        if (client.player == null) return
        
        val config = BazaarFlipperMod.CONFIG
        val stats = BazaarFlipperMod.FLIPPING_MANAGER.stats
        
        val hudWidth = 160
        val hudHeight = 80
        val x = config.hudX
        val y = config.hudY
        
        // Hintergrund zeichnen
        drawContext.fill(x, y, x + hudWidth, y + hudHeight, 0x80000000)
        drawContext.drawBorder(x, y, hudWidth, hudHeight, 0xFFFFFFFF.toInt())
        
        // Titel
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal("§6Bazaar Flipper"),
            x + 5,
            y + 5,
            0xFFFFFF
        )
        
        // Status
        val statusText = if (BazaarFlipperMod.isFlipping) "§aAktiv" else "§cInaktiv"
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal(statusText),
            x + hudWidth - client.textRenderer.getWidth(statusText) - 5,
            y + 5,
            0xFFFFFF
        )
        
        // Statistiken
        val profit = formatter.format(stats.totalProfit)
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal("§7Gewinn: §f$profit Münzen"),
            x + 5,
            y + 20,
            0xFFFFFF
        )
        
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal("§7Flips: §f${stats.flipCount}"),
            x + 5,
            y + 32,
            0xFFFFFF
        )
        
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal("§7Letzter Flip: §f${stats.lastFlippedItem}"),
            x + 5,
            y + 44,
            0xFFFFFF
        )
        
        val lastProfit = formatter.format(stats.lastFlipProfit)
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal("§7Letzter Gewinn: §f$lastProfit"),
            x + 5,
            y + 56,
            0xFFFFFF
        )
        
        drawContext.drawTextWithShadow(
            client.textRenderer,
            Text.literal("§7Zeit seit letztem Flip: §f${stats.getTimeSinceLastFlip()}"),
            x + 5,
            y + 68,
            0xFFFFFF
        )
        
        // Dragging-Logik
        handleDragging(client, x, y, hudWidth, hudHeight)
    }
    
    private fun handleDragging(client: MinecraftClient, x: Int, y: Int, width: Int, height: Int) {
        if (client.currentScreen != null) return
        
        val mouseX = client.mouse.x.toInt()
        val mouseY = client.mouse.y.toInt()
        val isMouseDown = client.mouse.wasLeftButtonClicked
        
        if (isMouseDown && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (!isDragging) {
                isDragging = true
                dragOffsetX = mouseX - x
                dragOffsetY = mouseY - y
            }
        } else if (!isMouseDown) {
            isDragging = false
        }
        
        if (isDragging) {
            val newX = mouseX - dragOffsetX
            val newY = mouseY - dragOffsetY
            
            BazaarFlipperMod.CONFIG.hudX = newX
            BazaarFlipperMod.CONFIG.hudY = newY
            BazaarFlipperMod.MOD_SCOPE.launch {
                ModConfig.save(BazaarFlipperMod.CONFIG)
            }
        }
    }
}
    }
}
