package net.arvandor.numinoustreasury.profession;

import static java.util.logging.Level.SEVERE;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.character.RPKCharacterService;
import com.rpkit.core.service.Service;
import com.rpkit.core.service.Services;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NuminousProfessionService implements Service {

    private final NuminousTreasury plugin;
    private final NuminousCharacterProfessionRepository repository;

    private final List<NuminousProfession> professions;
    private final Map<Integer, String> characterProfessions;
    private final Map<Integer, Integer> characterProfessionExperience;

    public NuminousProfessionService(NuminousTreasury plugin, NuminousCharacterProfessionRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        File professionsFolder = new File(plugin.getDataFolder(), "professions");
        if (!professionsFolder.exists()) {
            professionsFolder.mkdirs();
            saveDefaultProfessions(professionsFolder);
        }
        this.professions = Arrays.stream(professionsFolder.listFiles()).map(professionFile -> {
            YamlConfiguration itemConfiguration = YamlConfiguration.loadConfiguration(professionFile);
            return itemConfiguration.getObject("profession", NuminousProfession.class);
        }).toList();
        plugin.getLogger().info("Loaded " + professions.size() + " professions");
        this.characterProfessions = new ConcurrentHashMap<>();
        this.characterProfessionExperience = new ConcurrentHashMap<>();
    }

    public NuminousProfession getProfessionById(String id) {
        return professions.stream().filter(profession -> profession.getId().equals(id)).findAny().orElse(null);
    }

    public NuminousProfession getProfessionByName(String name) {
        return professions.stream().filter(profession -> profession.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public List<NuminousProfession> getProfessions() {
        return professions;
    }

    public NuminousProfession getProfession(OfflinePlayer player) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return null;
        return getProfession(character);
    }

    public NuminousProfession getProfession(RPKCharacter character) {
        String professionId = characterProfessions.get(character.getId().getValue());
        if (professionId == null) return null;
        return getProfessionById(professionId);
    }

    public void setProfession(OfflinePlayer player, NuminousProfession profession, Runnable callback) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return;
        setProfession(character, profession, callback);
    }

    public void setProfession(RPKCharacter character, NuminousProfession profession, Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
           repository.setProfessionId(character.getId(), profession.getId());
           characterProfessions.put(character.getId().getValue(), profession.getId());
           characterProfessionExperience.put(character.getId().getValue(), 0);
           plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    public int getProfessionLevel(OfflinePlayer player) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return 0;
        return getProfessionLevel(character);
    }

    public int getProfessionLevel(RPKCharacter character) {
        return getLevelAtExperience(getProfessionExperience(character));
    }

    public int getProfessionExperience(OfflinePlayer player) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return 0;
        return getProfessionExperience(character);
    }

    public void setProfessionExperience(OfflinePlayer player, int experience, Runnable callback) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return;
        setProfessionExperience(character, experience, callback);
    }

    public void addProfessionExperience(OfflinePlayer player, int experience, ExperienceUpdateCallback callback) {
        RPKCharacter character = getRpkCharacter(player);
        if (character == null) return;
        addProfessionExperience(character, experience, callback);
    }

    public int getProfessionExperience(RPKCharacter character) {
        return characterProfessionExperience.get(character.getId().getValue());
    }

    public void setProfessionExperience(RPKCharacter character, int experience, Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            repository.setProfessionExperience(character.getId(), experience);
            characterProfessionExperience.put(character.getId().getValue(), experience);
            plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    public void addProfessionExperience(RPKCharacter character, int experience, ExperienceUpdateCallback callback) {
        int experienceForMaxLevel = getExperienceForLevel(getMaxLevel());
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            repository.getAndUpdate(character.getId(), (dsl, oldExperience) -> repository.setProfessionExperience(dsl, character.getId(), Math.min(oldExperience + experience, experienceForMaxLevel)), (oldExperience, newExperience) -> {
                characterProfessionExperience.put(character.getId().getValue(), newExperience);
                callback.invoke(oldExperience, newExperience);
            });
        });
    }

    public int getLevelAtExperience(int experience) {
        int level = 1;
        while (plugin.getConfig().contains("experience.levels." + (level + 1)) && experience >= getTotalExperienceForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public int getExperienceForLevel(int level) {
        if (level <= 1) return getTotalExperienceForLevel(level);
        return getTotalExperienceForLevel(level) - getTotalExperienceForLevel(level - 1);
    }

    public int getTotalExperienceForLevel(int level) {
        int totalExperienceForLevel = 0;
        for (int i = 1; i <= level; i++) {
            if (!plugin.getConfig().contains("experience.levels." + i)) break;
            totalExperienceForLevel += plugin.getConfig().getInt("experience.levels." + i);
        }
        return totalExperienceForLevel;
    }

    public int getMaxLevel() {
        int level = 1;
        while (plugin.getConfig().contains("experience.levels." + (level + 1))) {
            level++;
        }
        return level;
    }

    public void load(RPKCharacter character) {
        String professionId = repository.getProfessionId(character.getId());
        if (professionId != null) {
            characterProfessions.put(character.getId().getValue(), professionId);
        }
        int professionExperience = repository.getProfessionExperience(character.getId());
        characterProfessionExperience.put(character.getId().getValue(), professionExperience);
    }

    public void unload(RPKCharacter character) {
        characterProfessions.remove(character.getId().getValue());
        characterProfessionExperience.remove(character.getId().getValue());
    }

    private RPKCharacter getRpkCharacter(OfflinePlayer player) {
        RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
        if (minecraftProfileService == null) return null;
        RPKMinecraftProfile minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player);
        if (minecraftProfile == null) return null;
        RPKCharacterService characterService = Services.INSTANCE.get(RPKCharacterService.class);
        if (characterService == null) return null;
        return characterService.getPreloadedActiveCharacter(minecraftProfile);
    }

    private void saveDefaultProfessions(File professionsFolder) {
        saveCarpenter(professionsFolder);
        saveSmith(professionsFolder);
    }

    private void saveCarpenter(File professionsFolder) {
        File carpenterFile = new File(professionsFolder, "carpenter.yml");
        YamlConfiguration carpenterConfig = new YamlConfiguration();
        carpenterConfig.set("profession", new NuminousProfession(
                "carpenter",
                "Carpenter"
        ));
        try {
            carpenterConfig.save(carpenterFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save carpenter profession", exception);
        }
    }

    private void saveSmith(File professionsFolder) {
        File smithFile = new File(professionsFolder, "smith.yml");
        YamlConfiguration smithConfig = new YamlConfiguration();
        smithConfig.set("profession", new NuminousProfession(
                "smith",
                "Smith"
        ));
        try {
            smithConfig.save(smithFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save smith profession", exception);
        }
    }

}
