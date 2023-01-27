package net.kingdommc.darkages.numinoustreasury.recipe;

import com.rpkit.core.service.Service;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class NuminousRecipeService implements Service {

    private final NuminousTreasury plugin;
    private final List<NuminousRecipe> recipes;

    public NuminousRecipeService(NuminousTreasury plugin) {
        this.plugin = plugin;
        File recipesFolder = new File(plugin.getDataFolder(), "recipes");
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
        }
        this.recipes = Arrays.stream(recipesFolder.listFiles()).map(recipeFile -> {
            YamlConfiguration itemConfiguration = YamlConfiguration.loadConfiguration(recipeFile);
            return itemConfiguration.getObject("recipe", NuminousRecipe.class);
        }).toList();
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public List<NuminousRecipe> getRecipes() {
        return recipes;
    }
}
