package net.arvandor.numinoustreasury.web.recipe

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.item.NuminousItemCategory
import net.arvandor.numinoustreasury.item.NuminousRarity
import net.arvandor.numinoustreasury.recipe.NuminousRecipeService
import net.arvandor.numinoustreasury.web.error.ErrorResponse
import net.arvandor.numinoustreasury.web.item.ItemResponse
import net.arvandor.numinoustreasury.web.itemstack.NuminousItemStackResponse
import net.arvandor.numinoustreasury.web.node.RequiredProfessionResponse
import net.arvandor.numinoustreasury.web.stamina.StaminaCostResponse
import org.bukkit.Material
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.LEATHER
import org.bukkit.Material.PLAYER_HEAD
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.enum
import org.http4k.lens.int
import org.http4k.lens.string

fun recipesRoute(): ContractRoute {
    val nameQuery = Query.string().optional("name", "All or part of the name of the recipe")
    val ingredientQuery = Query.string().optional("ingredient", "The ID of one of the ingredients")
    val resultQuery = Query.string().optional("result", "The ID of one of the resulting items")
    val professionQuery = Query.string().optional("profession", "The ID of a profession")
    val levelGteQuery =
        Query.int().optional(
            "levelGte",
            "Filter for recipes with a level greater or equal to n. Ignored if no profession specified",
        )
    val levelLteQuery =
        Query.int().optional(
            "levelLte",
            "Filter for recipes with a level less or equal to n. Ignored if no profession specified",
        )
    val levelExactQuery =
        Query.int().optional(
            "level",
            "Filter for recipes with a level exactly equal to n. Ignored if no profession specified",
        )
    val experienceGteQuery = Query.int().optional("experienceGte", "Filter for recipes with experience greater or equal to n")
    val experienceLteQuery = Query.int().optional("experienceLte", "Filter for recipes with experience less or equal to n")
    val staminaCostQuery = Query.enum<StaminaCostResponse>().optional("staminaCost", "Filter for recipes by stamina cost")
    val workstationQuery = Query.enum<Material>().optional("workstation", "Filter for recipes that require a specific workstation")

    val spec =
        "/recipes" meta {
            summary = "Get a list of recipes"
            operationId = "listRecipes"
            queries += nameQuery
            queries += ingredientQuery
            queries += resultQuery
            queries += professionQuery
            queries += levelGteQuery
            queries += levelLteQuery
            queries += levelExactQuery
            queries += experienceGteQuery
            queries += experienceLteQuery
            queries += staminaCostQuery
            queries += workstationQuery
            returning(
                OK,
                RecipeResponse.listLens to
                    listOf(
                        RecipeResponse(
                            "backpack",
                            listOf(
                                NuminousItemStackResponse(
                                    ItemResponse(
                                        "backpack",
                                        "Backpack",
                                        listOf("A backpack"),
                                        listOf(NuminousItemCategory.ADVENTURING_GEAR),
                                        NuminousRarity.COMMON,
                                        "5lb",
                                        PLAYER_HEAD,
                                        27,
                                        true,
                                    ),
                                    1,
                                ),
                            ),
                            listOf(
                                NuminousItemStackResponse(
                                    ItemResponse(
                                        "leather",
                                        "Leather",
                                        listOf("A piece of leather"),
                                        listOf(NuminousItemCategory.CRAFTING_MATERIAL),
                                        NuminousRarity.COMMON,
                                        "1lb",
                                        LEATHER,
                                        0,
                                        false,
                                    ),
                                    5,
                                ),
                            ),
                            listOf(
                                RequiredProfessionResponse(
                                    "99481074-3f2b-430b-ae63-ba2b99d8220b",
                                    "Weaver",
                                    1,
                                ),
                            ),
                            3,
                            StaminaCostResponse.LOW,
                            CRAFTING_TABLE,
                            PLAYER_HEAD,
                        ),
                    ),
            )
        } bindContract GET

    fun handler(request: Request): Response {
        val name = nameQuery(request)
        val ingredient = ingredientQuery(request)
        val result = resultQuery(request)
        val profession = professionQuery(request)
        val levelGte = levelGteQuery(request)
        val levelLte = levelLteQuery(request)
        val levelExact = levelExactQuery(request)
        val experienceGte = experienceGteQuery(request)
        val experienceLte = experienceLteQuery(request)
        val staminaCost = staminaCostQuery(request)
        val workstation = workstationQuery(request)

        val recipeService =
            Services.INSTANCE.get(NuminousRecipeService::class.java)
                ?: return Response(INTERNAL_SERVER_ERROR).with(
                    ErrorResponse.lens of ErrorResponse("No recipe service available"),
                )
        val filter = fun(recipe: RecipeResponse): Boolean {
            if (name != null && !recipe.name.contains(name, ignoreCase = true)) {
                return false
            }
            if (ingredient != null && recipe.ingredients.none { it.itemType.id == ingredient }) {
                return false
            }
            if (result != null && recipe.results.none { it.itemType.id == result }) {
                return false
            }
            if (profession != null) {
                val requiredProfessionLevel = recipe.requiredProfessionLevel.singleOrNull { it.id == profession }
                if (requiredProfessionLevel == null) {
                    return false
                } else {
                    if (levelGte != null && requiredProfessionLevel.level < levelGte) {
                        return false
                    }
                    if (levelLte != null && requiredProfessionLevel.level > levelLte) {
                        return false
                    }
                    if (levelExact != null && requiredProfessionLevel.level != levelExact) {
                        return false
                    }
                }
            }
            if (experienceGte != null && recipe.experience < experienceGte) {
                return false
            }
            if (experienceLte != null && recipe.experience > experienceLte) {
                return false
            }
            if (staminaCost != null && recipe.stamina != staminaCost) {
                return false
            }
            if (workstation != null && recipe.workstation != workstation) {
                return false
            }
            return true
        }
        val recipes = recipeService.recipes.map { it.toResponse() }.filter(filter)
        return Response(OK).with(RecipeResponse.listLens of recipes)
    }

    return spec to ::handler
}
