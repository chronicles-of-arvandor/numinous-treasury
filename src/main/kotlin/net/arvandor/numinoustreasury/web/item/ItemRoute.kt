package net.arvandor.numinoustreasury.web.item

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.item.NuminousItemCategory.ADVENTURING_GEAR
import net.arvandor.numinoustreasury.item.NuminousItemService
import net.arvandor.numinoustreasury.item.NuminousRarity.COMMON
import net.arvandor.numinoustreasury.web.error.ErrorResponse
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

fun itemRoute(): ContractRoute {
    val id = Path.of("id", "The ID of the item")
    val spec =
        "/item" / id meta {
            summary = "Get an item's details"
            operationId = "getItem"
            returning(
                OK,
                ItemResponse.lens to
                    ItemResponse(
                        "backpack",
                        "Backpack",
                        listOf("A backpack"),
                        listOf(ADVENTURING_GEAR),
                        COMMON,
                        "5lb",
                        PLAYER_HEAD,
                        27,
                        true,
                    ),
            )
            returning(
                NOT_FOUND,
                ErrorResponse.lens to ErrorResponse("Item not found"),
            )
        } bindContract GET

    fun handler(id: String): HttpHandler =
        handle@{ request ->
            val itemService =
                Services.INSTANCE.get(NuminousItemService::class.java)
                    ?: return@handle Response(INTERNAL_SERVER_ERROR).with(
                        ErrorResponse.lens of ErrorResponse("No item service available"),
                    )
            val itemType =
                itemService.getItemTypeById(id)
                    ?: return@handle Response(NOT_FOUND).with(
                        ErrorResponse.lens of ErrorResponse("Item not found"),
                    )
            return@handle Response(OK).with(
                ItemResponse.lens of itemType.toResponse(),
            )
        }

    return spec to ::handler
}
