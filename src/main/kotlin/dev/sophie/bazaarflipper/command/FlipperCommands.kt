package dev.sophie.bazaarflipper.command

import com.mojang.brigadier.arguments.StringArgumentType
import dev.sophie.bazaarflipper.BazaarFlipperMod
import dev.sophie.bazaarflipper.config.ModConfig
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.text.Text
import java.text.DecimalFormat

fun registerCommands() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
        dispatcher.register(
            ClientCommandManager.literal("flipper")
                .then(
                    ClientCommandManager.literal("start")
                        .executes { context ->
                            val player = context.source.player
                            if (!BazaarFlipperMod.isFlipping) {
                                BazaarFlipperMod.isFlipping = true
                                player.sendMessage(Text.literal("§aBazaar Flipper gestartet."))
                            } else {
                                player.sendMessage(Text.literal("§cBazaar Flipper läuft bereits."))
                            }
                            1
                        }
                )
                .then(
                    ClientCommandManager.literal("stop")
                        .executes { context ->
                            val player = context.source.player
                            if (BazaarFlipperMod.isFlipping) {
                                BazaarFlipperMod.isFlipping = false
                                player.sendMessage(Text.literal("§cBazaar Flipper gestoppt."))
                            } else {
                                player.sendMessage(Text.literal("§cBazaar Flipper läuft nicht."))
                            }
                            1
                        }
                )
                .then(
                    ClientCommandManager.literal("status")
                        .executes { context ->
                            val player = context.source.player
                            val stats = BazaarFlipperMod.FLIPPING_MANAGER.stats
                            val formatter = DecimalFormat("#,###.##")
                            
                            player.sendMessage(Text.literal("§6--- Bazaar Flipper Status ---"))
                            player.sendMessage(Text.literal("§7Status: ${if (BazaarFlipperMod.isFlipping) "§aAktiv" else "§cInaktiv"}"))
                            player.sendMessage(Text.literal("§7Gesamtgewinn: §f${formatter.format(stats.totalProfit)} Münzen"))
                            player.sendMessage(Text.literal("§7Anzahl Flips: §f${stats.flipCount}"))
                            player.sendMessage(Text.literal("§7Letzter Flip: §f${stats.lastFlippedItem}"))
                            player.sendMessage(Text.literal("§7Letzter Gewinn: §f${formatter.format(stats.lastFlipProfit)} Münzen"))
                            player.sendMessage(Text.literal("§7Zeit seit letztem Flip: §f${stats.getTimeSinceLastFlip()}"))
                            1
                        }
                )
                .then(
                    ClientCommandManager.literal("setkey")
                        .then(
                            ClientCommandManager.argument("key", StringArgumentType.string())
                                .executes { context ->
                                    val player = context.source.player
                                    val key = StringArgumentType.getString(context, "key")
                                    
                                    BazaarFlipperMod.API_CLIENT.updateApiKey(key)
                                    BazaarFlipperMod.CONFIG.apiKey = key
                                    BazaarFlipperMod.MOD_SCOPE.launch {
                                        ModConfig.save(BazaarFlipperMod.CONFIG)
                                    }
                                    
                                    player.sendMessage(Text.literal("§aAPI-Schlüssel aktualisiert."))
                                    1
                                }
                        )
                )
                .then(
                    ClientCommandManager.literal("gui")
                        .then(
                            ClientCommandManager.literal("toggle")
                                .executes { context ->
                                    val player = context.source.player
                                    BazaarFlipperMod.showHud = !BazaarFlipperMod.showHud
                                    
                                    player.sendMessage(
                                        Text.literal(
                                            if (BazaarFlipperMod.showHud) "§aHUD angezeigt."
                                            else "§cHUD ausgeblendet."
                                        )
                                    )
                                    1
                                }
                        )
                )
                .executes { context ->
                    val player = context.source.player
                    player.sendMessage(Text.literal("§6--- Bazaar Flipper Hilfe ---"))
                    player.sendMessage(Text.literal("§f/flipper start §7- Startet den Flipper"))
                    player.sendMessage(Text.literal("§f/flipper stop §7- Stoppt den Flipper"))
                    player.sendMessage(Text.literal("§f/flipper status §7- Zeigt Status und Statistiken"))
                    player.sendMessage(Text.literal("§f/flipper setkey <schlüssel> §7- Setzt den API-Schlüssel"))
                    player.sendMessage(Text.literal("§f/flipper gui toggle §7- HUD ein-/ausblenden"))
                    1
                }
        )
    }
}
