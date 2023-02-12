package net.kingdommc.darkages.numinoustreasury.listener;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent;
import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.kingdommc.darkages.numinoustreasury.stamina.NuminousStaminaService;
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
        staminaService.unload(event.getFromCharacter());
        staminaService.load(event.getCharacter());
    }

}
