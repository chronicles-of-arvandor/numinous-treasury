package net.kingdommc.darkages.numinoustreasury.listener;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.character.RPKCharacterService;
import com.rpkit.core.service.Services;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.kingdommc.darkages.numinoustreasury.stamina.NuminousStaminaService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public final class AsyncPlayerPreLoginListener implements Listener {

    private final NuminousTreasury plugin;

    public AsyncPlayerPreLoginListener(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
        if (minecraftProfileService == null) {
            plugin.getLogger().info("No Minecraft profile service found. Could not load profession info.");
            return;
        }
        RPKMinecraftProfile minecraftProfile = minecraftProfileService.getMinecraftProfile(event.getUniqueId()).join();
        if (minecraftProfile == null) return;

        RPKCharacterService characterService = Services.INSTANCE.get(RPKCharacterService.class);
        if (characterService == null) {
            plugin.getLogger().info("No character service found. Could not load profession info.");
            return;
        }
        RPKCharacter character = characterService.getActiveCharacter(minecraftProfile).join();
        if (character == null) return;

        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        professionService.load(character);

        NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
        staminaService.load(character);
    }

}
