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
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters.Cors
import org.http4k.lens.Query
import org.http4k.lens.enum
import org.http4k.lens.int
import org.http4k.lens.string

fun itemsRoute(method: Method): ContractRoute {
    val nameQuery = Query.string().optional("name", "All or part of the name of the item")
    val categoryQuery = Query.enum<NuminousItemCategory>().multi.optional("category", "The category of the item")
    val rarityQuery = Query.enum<NuminousRarity>().multi.optional("rarity", "The rarity of the item")
    val offsetQuery = Query.int().optional("offset", "The number of items to skip")
    val limitQuery = Query.int().optional("limit", "The maximum number of items to return")
    val spec =
        "/items" meta {
            summary = "Get a list of items"
            operationId = "listItems"
            queries += nameQuery
            queries += categoryQuery
            queries += rarityQuery
            queries += offsetQuery
            queries += limitQuery
            returning(
                OK,
                ItemResponse.listLens to
                    listOf(
                        ItemResponse(
                            "backpack",
                            "Backpack",
                            listOf("A backpack"),
                            listOf(ADVENTURING_GEAR),
                            NuminousRarity.COMMON,
                            "5lb",
                            PLAYER_HEAD,
                            27,
                            true,
                        ),
                    ),
            )
        } bindContract method

    fun handler(request: Request): Response {
        val name = nameQuery(request)
        val categories = categoryQuery(request)
        val rarities = rarityQuery(request)
        val offset = offsetQuery(request)
        val limit = limitQuery(request)
        val itemService =
            Services.INSTANCE.get(NuminousItemService::class.java)
                ?: return Response(INTERNAL_SERVER_ERROR).with(
                    ErrorResponse.lens of ErrorResponse("No item service available"),
                )
        val filter = fun(itemType: NuminousItemType): Boolean {
            if (name != null && !itemType.name.contains(name, ignoreCase = true)) {
                return false
            }
            if (categories != null && itemType.categories.none { categories.contains(it) }) {
                return false
            }
            if (rarities != null && !rarities.contains(itemType.rarity)) {
                return false
            }
            return true
        }
        val items =
            itemService.itemTypes.filter(filter)
                .drop(offset ?: 0)
                .take(limit ?: Int.MAX_VALUE)
        return Response(OK).with(ItemResponse.listLens of items.map { it.toResponse() })
    }

    return spec to
        Cors(
            CorsPolicy(
                originPolicy = OriginPolicy.AllowAll(),
                headers = emptyList(),
                methods = listOf(GET),
            ),
        ).then(::handler)
}
