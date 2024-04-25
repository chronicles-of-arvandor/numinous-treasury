package net.arvandor.numinoustreasury.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: NuminousTreasury) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                val minecraftProfileService =
                    Services.INSTANCE.get(
                        RPKMinecraftProfileService::class.java,
                    )
                if (minecraftProfileService == null) {
                    plugin.logger.info("No Minecraft profile service found. Could not load profession info.")
                    return@Runnable
                }
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player).join()
                val characterService =
                    Services.INSTANCE.get(
                        RPKCharacterService::class.java,
                    )
                if (characterService == null) {
                    plugin.logger.info("No character service found. Could not load profession info.")
                    return@Runnable
                }
                val character = characterService.getActiveCharacter(minecraftProfile).join()
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        if (!minecraftProfile.isOnline) {
                            if (character != null) {
                                val professionService =
                                    Services.INSTANCE.get(
                                        NuminousProfessionService::class.java,
                                    )
                                professionService.unload(character)
                                val staminaService =
                                    Services.INSTANCE.get(
                                        NuminousStaminaService::class.java,
                                    )
                                staminaService.unload(character)
                            }
                        }
                    },
                )
            },
        )
    }
}
