package net.arvandor.numinoustreasury.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent
import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterSwitchListener : Listener {
    @EventHandler
    fun onCharacterSwitch(event: RPKBukkitCharacterSwitchEvent) {
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        val fromCharacter = event.fromCharacter
        if (fromCharacter != null) {
            professionService.unload(fromCharacter)
        }
        val toCharacter = event.character
        if (toCharacter != null) {
            professionService.load(toCharacter)
        }

        val staminaService =
            Services.INSTANCE.get(
                NuminousStaminaService::class.java,
            )
        if (fromCharacter != null) {
            staminaService.unload(event.fromCharacter)
        }
        if (toCharacter != null) {
            staminaService.load(event.character)
        }
    }
}
