package net.arvandor.numinoustreasury.recipe

import com.rpkit.core.service.Service
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class NuminousRecipeService(private val plugin: NuminousTreasury) : Service {
    val recipes: List<NuminousRecipe>

    init {
        val recipesFolder = File(plugin.dataFolder, "recipes")
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs()
        }
        this.recipes =
            recipesFolder.listFiles().mapNotNull { recipeFile ->
                val itemConfiguration = YamlConfiguration.loadConfiguration(recipeFile)
                val recipe = itemConfiguration.getObject("recipe", NuminousRecipe::class.java)
                if (recipe == null) {
                    plugin.logger.warning("Failed to load recipe from " + recipeFile.name)
                    return@mapNotNull null
                }
                return@mapNotNull recipe
            }
        plugin.logger.info("Loaded " + recipes.size + " recipes")
    }

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }
}
