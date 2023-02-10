package net.kingdommc.darkages.numinoustreasury.listener;

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterSwitchEvent;
import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.kingdommc.darkages.numinoustreasury.stamina.NuminousStaminaService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RPKCharacterSwitchListener implements Listener {

    @EventHandler
    public void onCharacterSwitch(RPKBukkitCharacterSwitchEvent event) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        professionService.unload(event.getFromCharacter());
        professionService.load(event.getCharacter());

        NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
        staminaService.unload(event.getFromCharacter());
        staminaService.load(event.getCharacter());
    }

}
