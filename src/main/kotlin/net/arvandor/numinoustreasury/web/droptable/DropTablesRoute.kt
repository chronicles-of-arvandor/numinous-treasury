package net.arvandor.numinoustreasury.web.droptable

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService
import net.arvandor.numinoustreasury.item.NuminousItemCategory.CRAFTING_MATERIAL
import net.arvandor.numinoustreasury.item.NuminousRarity
import net.arvandor.numinoustreasury.item.NuminousRarity.COMMON
import net.arvandor.numinoustreasury.web.error.ErrorResponse
import net.arvandor.numinoustreasury.web.item.ItemResponse
import net.arvandor.numinoustreasury.web.itemstack.NuminousItemStackResponse
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.STONE
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with

fun dropTablesRoute(): ContractRoute {
    val spec =
        "/drop-tables" meta {
            summary = "Get a list of drop tables"
            operationId = "listDropTables"
            returning(
                OK,
                DropTableResponse.listLens to
                    listOf(
                        DropTableResponse(
                            "mining_1",
                            listOf(
                                DropTableItemResponse(
                                    listOf(
                                        NuminousItemStackResponse(
                                            ItemResponse(
                                                "stone",
                                                "Stone",
                                                listOf(CRAFTING_MATERIAL),
                                                COMMON,
                                                "5lb",
                                                STONE,
                                                0,
                                                false,
                                            ),
                                            1,
                                        ),
                                    ),
                                    75.0,
                                ),
                                DropTableItemResponse(
                                    listOf(
                                        NuminousItemStackResponse(
                                            ItemResponse(
                                                "iron_ore",
                                                "Iron Ore",
                                                listOf(CRAFTING_MATERIAL),
                                                NuminousRarity.RARE,
                                                "5lb",
                                                IRON_ORE,
                                                0,
                                                false,
                                            ),
                                            1,
                                        ),
                                    ),
                                    25.0,
                                ),
                            ),
                        ),
                    ),
            )
        } bindContract GET

    fun handler(request: Request): Response {
        val dropTableService =
            Services.INSTANCE.get(NuminousDropTableService::class.java)
                ?: return Response(INTERNAL_SERVER_ERROR).with(
                    ErrorResponse.lens of ErrorResponse("No drop table service available"),
                )
        val dropTables = dropTableService.dropTables
        return Response(OK).with(DropTableResponse.listLens of dropTables.map { it.toResponse() })
    }

    return spec to ::handler
}
