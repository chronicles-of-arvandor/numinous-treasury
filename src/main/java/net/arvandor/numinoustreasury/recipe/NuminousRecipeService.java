package net.arvandor.numinoustreasury.recipe;

import com.rpkit.core.service.Service;
import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.NuminousItemService;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.logging.Level.SEVERE;
import static org.bukkit.Material.*;

public final class NuminousRecipeService implements Service {

    private final NuminousTreasury plugin;
    private final List<NuminousRecipe> recipes;

    public NuminousRecipeService(NuminousTreasury plugin) {
        this.plugin = plugin;
        File recipesFolder = new File(plugin.getDataFolder(), "recipes");
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
            saveExampleRecipes(recipesFolder);
        }
        this.recipes = Arrays.stream(recipesFolder.listFiles()).map(recipeFile -> {
            YamlConfiguration itemConfiguration = YamlConfiguration.loadConfiguration(recipeFile);
            return itemConfiguration.getObject("recipe", NuminousRecipe.class);
        }).toList();
        plugin.getLogger().info("Loaded " + recipes.size() + " recipes");
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public List<NuminousRecipe> getRecipes() {
        return recipes;
    }

    private void saveExampleRecipes(File recipesFolder) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        saveHandleRecipe(recipesFolder, itemService, professionService);
        savePigIronIngotRecipe(recipesFolder, itemService, professionService);
        saveSteelIngotRecipe(recipesFolder, itemService, professionService);
        saveBladeRecipe(recipesFolder, itemService, professionService);
        saveSteelSwordRecipe(recipesFolder, itemService, professionService);
    }

    private void saveHandleRecipe(File recipesFolder, NuminousItemService itemService, NuminousProfessionService professionService) {
        File handleFile = new File(recipesFolder, "handle.yml");
        YamlConfiguration handleConfig = new YamlConfiguration();
        handleConfig.set("recipe", new NuminousRecipe(
                "Handle",
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("wood"))
                ),
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("handle"), 4)
                ),
                Map.of(
                        professionService.getProfessionById("carpenter"), 1
                ),
                5,
                5,
                CRAFTING_TABLE,
                STICK
        ));
        try {
            handleConfig.save(handleFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save handle recipe", exception);
        }
    }

    private void savePigIronIngotRecipe(File recipesFolder, NuminousItemService itemService, NuminousProfessionService professionService) {
        File pigIronIngotFile = new File(recipesFolder, "pig_iron_ingot.yml");
        YamlConfiguration pigIronIngotConfig = new YamlConfiguration();
        pigIronIngotConfig.set("recipe", new NuminousRecipe(
                "Pig iron ingot",
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("iron_ore"))
                ),
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("pig_iron_ingot"))
                ),
                Map.of(
                        professionService.getProfessionById("smith"), 1
                ),
                5,
                5,
                BLAST_FURNACE,
                IRON_INGOT
        ));
        try {
            pigIronIngotConfig.save(pigIronIngotFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save pig iron ingot recipe", exception);
        }
    }

    private void saveSteelIngotRecipe(File recipesFolder, NuminousItemService itemService, NuminousProfessionService professionService) {
        File steelIngotFile = new File(recipesFolder, "steel_ingot.yml");
        YamlConfiguration steelIngotConfig = new YamlConfiguration();
        steelIngotConfig.set("recipe", new NuminousRecipe(
                "Steel ingot",
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("pig_iron_ingot")),
                        new NuminousItemStack(itemService.getItemTypeById("carbon"))
                ),
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("steel_ingot"))
                ),
                Map.of(
                        professionService.getProfessionById("smith"), 2
                ),
                10,
                10,
                FURNACE,
                IRON_INGOT
        ));
        try {
            steelIngotConfig.save(steelIngotFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save steel ingot recipe", exception);
        }
    }

    private void saveBladeRecipe(File recipesFolder, NuminousItemService itemService, NuminousProfessionService professionService) {
        File bladeFile = new File(recipesFolder, "blade.yml");
        YamlConfiguration bladeConfig = new YamlConfiguration();
        bladeConfig.set("recipe", new NuminousRecipe(
                "Blade",
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("steel_ingot"), 4)
                ),
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("blade"))
                ),
                Map.of(
                        professionService.getProfessionById("smith"), 5
                ),
                15,
                15,
                FURNACE,
                IRON_INGOT
        ));
        try {
            bladeConfig.save(bladeFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save blade recipe", exception);
        }
    }

    private void saveSteelSwordRecipe(File recipesFolder, NuminousItemService itemService, NuminousProfessionService professionService) {
        File steelSwordFile = new File(recipesFolder, "steel_sword.yml");
        YamlConfiguration steelSwordConfig = new YamlConfiguration();
        steelSwordConfig.set("recipe", new NuminousRecipe(
                "Steel sword",
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("blade")),
                        new NuminousItemStack(itemService.getItemTypeById("handle"))
                ),
                List.of(
                        new NuminousItemStack(itemService.getItemTypeById("steel_sword"))
                ),
                Map.of(
                        professionService.getProfessionById("smith"), 10
                ),
                20,
                20,
                FURNACE,
                IRON_SWORD
        ));
        try {
            steelSwordConfig.save(steelSwordFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save steel sword recipe", exception);
        }
    }
}
