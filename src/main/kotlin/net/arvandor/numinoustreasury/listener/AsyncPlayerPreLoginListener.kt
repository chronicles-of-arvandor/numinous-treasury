package net.arvandor.numinoustreasury.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener(private val plugin: NuminousTreasury) : Listener {
    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService =
            Services.INSTANCE.get(
                RPKMinecraftProfileService::class.java,
            )
        if (minecraftProfileService == null) {
            plugin.logger.info("No Minecraft profile service found. Could not load profession info.")
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join() ?: return

        val characterService =
            Services.INSTANCE.get(
                RPKCharacterService::class.java,
            )
        if (characterService == null) {
            plugin.logger.info("No character service found. Could not load profession info.")
            return
        }
        val character = characterService.getActiveCharacter(minecraftProfile).join() ?: return

        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        professionService.load(character)

        val staminaService =
            Services.INSTANCE.get(
                NuminousStaminaService::class.java,
            )
        staminaService.load(character)
    }
}
