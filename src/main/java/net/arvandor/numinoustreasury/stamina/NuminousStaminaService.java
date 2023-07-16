package net.arvandor.numinoustreasury.stamina;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.character.RPKCharacterService;
import com.rpkit.core.service.Service;
import com.rpkit.core.service.Services;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class NuminousStaminaService implements Service {

    private final NuminousTreasury plugin;
    private final NuminousCharacterStaminaRepository staminaRepository;
    private final Map<Integer, Integer> characterStamina;

    public NuminousStaminaService(NuminousTreasury plugin, NuminousCharacterStaminaRepository staminaRepository) {
        this.plugin = plugin;
        this.staminaRepository = staminaRepository;
        this.characterStamina = new ConcurrentHashMap<>();
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public int getStamina(RPKCharacter character) {
        return characterStamina.get(character.getId().getValue());
    }

    public int getStamina(Player player) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return 0;
        return getStamina(character);
    }

    public void getAndUpdateStamina(Player player, StaminaUpdateFunction function, StaminaUpdateCallback callback) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return;
        getAndUpdateStamina(character, function, callback);
    }

    public void getAndUpdateStamina(RPKCharacter character, StaminaUpdateFunction function, StaminaUpdateCallback callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            staminaRepository.getAndUpdate(character.getId(), function, (oldStamina, newStamina) -> {
                characterStamina.put(character.getId().getValue(), newStamina);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    callback.invoke(oldStamina, newStamina);
                });
            });
        });
    }

    public void setStamina(Player player, int stamina, Runnable callback) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return;
        setStamina(character, stamina, callback);
    }

    public void setStamina(RPKCharacter character, int stamina, Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            staminaRepository.setStamina(character.getId(), stamina);
            characterStamina.put(character.getId().getValue(), stamina);
            plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    public void setStamina(DSLContext dsl, Player player, int stamina) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return;
        setStamina(dsl, character, stamina);
    }

    public void setStamina(DSLContext dsl, RPKCharacter character, int stamina) {
        staminaRepository.setStamina(dsl, character.getId(), stamina);
        characterStamina.put(character.getId().getValue(), stamina);
    }

    public void restoreStamina(Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
           staminaRepository.restoreStamina();
           characterStamina.clear();
           plugin.getServer().getScheduler().runTask(plugin, () -> {
               List<RPKCharacter> onlineCharacters = plugin.getServer().getOnlinePlayers().stream()
                       .map(this::getRpkCharacter)
                       .filter(Objects::nonNull)
                       .toList();
               plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                   onlineCharacters.forEach(this::load);
                   plugin.getServer().getScheduler().runTask(plugin, callback);
               });
           });
        });
    }

    private RPKCharacter getRpkCharacter(Player player) {
        RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
        if (minecraftProfileService == null) return null;
        RPKMinecraftProfile minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player);
        if (minecraftProfile == null) return null;
        RPKCharacterService characterService = Services.INSTANCE.get(RPKCharacterService.class);
        if (characterService == null) return null;
        return characterService.getPreloadedActiveCharacter(minecraftProfile);
    }

    public void load(RPKCharacter character) {
        characterStamina.put(character.getId().getValue(), staminaRepository.getStamina(character.getId()));
    }

    public void unload(RPKCharacter character) {
        characterStamina.remove(character.getId().getValue());
    }

}
