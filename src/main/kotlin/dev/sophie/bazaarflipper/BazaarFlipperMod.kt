package dev.sophie.bazaarflipper

import dev.sophie.bazaarflipper.api.HypixelApiClient
import dev.sophie.bazaarflipper.command.registerCommands
import dev.sophie.bazaarflipper.config.ModConfig
import dev.sophie.bazaarflipper.gui.HudOverlay
import dev.sophie.bazaarflipper.flipping.FlippingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import org.slf4j.LoggerFactory

class BazaarFlipperMod : ClientModInitializer {
    companion object {
        const val MOD_ID = "bazaarflipper"
        val LOGGER = LoggerFactory.getLogger("BazaarFlipper")
        val MOD_SCOPE = CoroutineScope(Dispatchers.IO + SupervisorJob())
        
        lateinit var CONFIG: ModConfig
        lateinit var API_CLIENT: HypixelApiClient
        lateinit var FLIPPING_MANAGER: FlippingManager
        lateinit var HUD_OVERLAY: HudOverlay
        
        var isFlipping = false
        var showHud = true
    }

    override fun onInitializeClient() {
        LOGGER.info("Initialisiere Bazaar Flipper Mod")
        
        CONFIG = ModConfig.load()
        API_CLIENT = HypixelApiClient(CONFIG.apiKey)
        FLIPPING_MANAGER = FlippingManager()
        HUD_OVERLAY = HudOverlay()
        
        registerCommands()
        
        HudRenderCallback.EVENT.register(HUD_OVERLAY)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (isFlipping) {
                FLIPPING_MANAGER.tick(client)
            }
        }
        
        LOGGER.info("Bazaar Flipper Mod erfolgreich initialisiert")
    }
}
