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
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters.Cors
import org.http4k.lens.Path

fun dropTableRoute(): ContractRoute {
    val id = Path.of("id", "The ID of the drop table")
    val spec =
        "/drop-table" / id meta {
            summary = "Get a drop table's details"
            operationId = "getDropTable"
            returning(
                OK,
                DropTableResponse.lens to
                    DropTableResponse(
                        "mining_1",
                        listOf(
                            DropTableItemResponse(
                                listOf(
                                    NuminousItemStackResponse(
                                        ItemResponse(
                                            "stone",
                                            "Stone",
                                            listOf("A stone"),
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
                                            listOf("Some iron ore"),
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
            )
            returning(
                NOT_FOUND,
                ErrorResponse.lens to ErrorResponse("Drop table not found"),
            )
        } bindContract GET

    fun handler(id: String): HttpHandler =
        Cors(
            CorsPolicy(
                originPolicy = OriginPolicy.AllowAll(),
                headers = emptyList(),
                methods = listOf(GET),
            ),
        ).then handle@{ request ->
            val dropTableService =
                Services.INSTANCE.get(NuminousDropTableService::class.java)
                    ?: return@handle Response(INTERNAL_SERVER_ERROR).with(
                        ErrorResponse.lens of ErrorResponse("No drop table service available"),
                    )
            val dropTable =
                dropTableService.getDropTableById(id)
                    ?: return@handle Response(NOT_FOUND).with(
                        ErrorResponse.lens of ErrorResponse("Drop table not found"),
                    )
            return@handle Response(OK).with(
                DropTableResponse.lens of dropTable.toResponse(),
            )
        }

    return spec to ::handler
}
