package net.arvandor.numinoustreasury.web.item

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.item.NuminousItemCategory
import net.arvandor.numinoustreasury.item.NuminousItemCategory.ADVENTURING_GEAR
import net.arvandor.numinoustreasury.item.NuminousItemService
import net.arvandor.numinoustreasury.item.NuminousItemType
import net.arvandor.numinoustreasury.item.NuminousRarity
import net.arvandor.numinoustreasury.web.error.ErrorResponse
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
import org.http4k.lens.string

fun itemsRoute(): ContractRoute {
    val nameQuery = Query.string().optional("name", "All or part of the name of the item")
    val categoryQuery = Query.enum<NuminousItemCategory>().optional("category", "The category of the item")
    val rarityQuery = Query.enum<NuminousRarity>().optional("rarity", "The rarity of the item")
    val spec =
        "/items" meta {
            summary = "Get a list of items"
            operationId = "listItems"
            queries += nameQuery
            queries += categoryQuery
            queries += rarityQuery
            returning(
                OK,
                ItemResponse.listLens to
                    listOf(
                        ItemResponse(
                            "backpack",
                            "Backpack",
                            listOf(ADVENTURING_GEAR),
                            NuminousRarity.COMMON,
                            "5lb",
                            PLAYER_HEAD,
                            27,
                            true,
                        ),
                    ),
            )
        } bindContract GET

    fun handler(request: Request): Response {
        val name = nameQuery(request)
        val category = categoryQuery(request)
        val rarity = rarityQuery(request)
        val itemService =
            Services.INSTANCE.get(NuminousItemService::class.java)
                ?: return Response(INTERNAL_SERVER_ERROR).with(
                    ErrorResponse.lens of ErrorResponse("No item service available"),
                )
        val filter = fun(itemType: NuminousItemType): Boolean {
            if (name != null && !itemType.name.contains(name, ignoreCase = true)) {
                return false
            }
            if (category != null && itemType.categories.none { it == category }) {
                return false
            }
            if (rarity != null && itemType.rarity != rarity) {
                return false
            }
            return true
        }
        val items = itemService.itemTypes.filter(filter)
        return Response(OK).with(ItemResponse.listLens of items.map { it.toResponse() })
    }

    return spec to ::handler
}
