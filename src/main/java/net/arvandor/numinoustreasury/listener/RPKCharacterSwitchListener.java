package net.arvandor.numinoustreasury.listener;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent;
import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class RPKCharacterSwitchListener implements Listener {

    @EventHandler
    public void onCharacterSwitch(RPKBukkitCharacterSwitchEvent event) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        RPKCharacter fromCharacter = event.getFromCharacter();
        if (fromCharacter != null) {
            professionService.unload(fromCharacter);
        }
        RPKCharacter toCharacter = event.getCharacter();
        if (toCharacter != null) {
            professionService.load(toCharacter);
        }

        NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
        if (fromCharacter != null) {
            staminaService.unload(event.getFromCharacter());
        }
        if (toCharacter != null) {
            staminaService.load(event.getCharacter());
        }
    }

}
