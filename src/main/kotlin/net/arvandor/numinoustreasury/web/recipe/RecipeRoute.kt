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
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.LEATHER
import org.bukkit.Material.PLAYER_HEAD
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path

fun recipeRoute(): ContractRoute {
    val name = Path.of("name", "The name of the recipe")
    val spec =
        "/recipe" / name meta {
            summary = "Get a recipe's details"
            operationId = "getRecipe"
            returning(
                OK,
                RecipeResponse.lens to
                    RecipeResponse(
                        "backpack",
                        listOf(
                            NuminousItemStackResponse(
                                ItemResponse(
                                    "backpack",
                                    "Backpack",
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
            )
            returning(
                NOT_FOUND,
                ErrorResponse.lens to ErrorResponse("Recipe not found"),
            )
        } bindContract GET

    fun handler(name: String): HttpHandler =
        handle@{ request ->
            val recipeService =
                Services.INSTANCE.get(NuminousRecipeService::class.java)
                    ?: return@handle Response(INTERNAL_SERVER_ERROR).with(
                        ErrorResponse.lens of ErrorResponse("No recipe service available"),
                    )
            val recipe =
                recipeService.recipes.singleOrNull { it.name == name }
                    ?: return@handle Response(NOT_FOUND).with(
                        ErrorResponse.lens of ErrorResponse("Recipe not found"),
                    )
            return@handle Response(OK).with(
                RecipeResponse.lens of recipe.toResponse(),
            )
        }

    return spec to ::handler
}
