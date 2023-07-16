package net.arvandor.numinoustreasury.listener;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.character.RPKCharacterService;
import com.rpkit.core.service.Services;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final NuminousTreasury plugin;

    public PlayerQuitListener(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
            if (minecraftProfileService == null) {
                plugin.getLogger().info("No Minecraft profile service found. Could not load profession info.");
                return;
            }
            RPKMinecraftProfile minecraftProfile = minecraftProfileService.getMinecraftProfile(event.getPlayer()).join();
            RPKCharacterService characterService = Services.INSTANCE.get(RPKCharacterService.class);
            if (characterService == null) {
                plugin.getLogger().info("No character service found. Could not load profession info.");
                return;
            }
            RPKCharacter character = characterService.getActiveCharacter(minecraftProfile).join();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!minecraftProfile.isOnline()) {
                    NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
                    professionService.unload(character);
                    NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
                    staminaService.unload(character);
                }
            });
        });
    }

}
