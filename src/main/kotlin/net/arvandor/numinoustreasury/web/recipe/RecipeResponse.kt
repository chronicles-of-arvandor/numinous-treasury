package net.arvandor.numinoustreasury.web.recipe

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.recipe.NuminousRecipe
import net.arvandor.numinoustreasury.recipe.NuminousRecipeService
import net.arvandor.numinoustreasury.web.itemstack.NuminousItemStackResponse
import net.arvandor.numinoustreasury.web.itemstack.toResponse
import net.arvandor.numinoustreasury.web.node.RequiredProfessionResponse
import net.arvandor.numinoustreasury.web.stamina.StaminaCostResponse
import org.bukkit.Material
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class RecipeResponse(
    val name: String,
    val ingredients: List<NuminousItemStackResponse>,
    val results: List<NuminousItemStackResponse>,
    val requiredProfessionLevel: List<RequiredProfessionResponse>,
    val experience: Int,
    val stamina: StaminaCostResponse,
    val workstation: Material,
    val iconMaterial: Material,
) {
    companion object {
        val lens = Body.auto<RecipeResponse>().toLens()
        val listLens = Body.auto<List<RecipeResponse>>().toLens()
    }
}

fun NuminousRecipe.toResponse(): RecipeResponse {
    val recipeService =
        Services.INSTANCE.get(NuminousRecipeService::class.java)
            ?: throw IllegalStateException("No NuminousRecipeService found")
    val recipes = recipeService.recipes
    val staminaLowerQuartile = recipes.map { it.stamina }.sorted()[recipes.size / 4]
    val staminaUpperQuartile = recipes.map { it.stamina }.sorted()[recipes.size * 3 / 4]
    val staminaCost =
        when {
            stamina < staminaLowerQuartile -> StaminaCostResponse.LOW
            stamina < staminaUpperQuartile -> StaminaCostResponse.MEDIUM
            else -> StaminaCostResponse.HIGH
        }
    return RecipeResponse(
        name = name,
        ingredients = ingredients.map { it.toResponse() },
        results = results.map { it.toResponse() },
        requiredProfessionLevel =
            requiredProfessionLevel.map { (profession, level) ->
                RequiredProfessionResponse(
                    id = profession.id,
                    name = profession.name,
                    level = level,
                )
            },
        experience = experience,
        stamina = staminaCost,
        workstation = workstation,
        iconMaterial = iconMaterial,
    )
}
