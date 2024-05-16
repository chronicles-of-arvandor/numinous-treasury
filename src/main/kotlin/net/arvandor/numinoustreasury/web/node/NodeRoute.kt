package net.arvandor.numinoustreasury.web.node

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.web.error.ErrorResponse
import net.arvandor.numinoustreasury.web.stamina.StaminaCostResponse
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.HttpHandler
import org.http4k.core.Method
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

fun nodeRoute(method: Method): ContractRoute {
    val id = Path.of("id", "The ID of the node")
    val spec =
        "/node" / id meta {
            summary = "Get a node's details"
            operationId = "getNode"
            returning(
                OK,
                NodeResponse.lens to
                    NodeResponse(
                        "c9d6e1b0-c86a-4853-bee8-69097f8775c3",
                        "Mining 1",
                        listOf(
                            RequiredProfessionResponse(
                                "99481074-3f2b-430b-ae63-ba2b99d8220b",
                                "Miner",
                                1,
                            ),
                        ),
                        3,
                        StaminaCostResponse.LOW,
                        "mining_1",
                    ),
            )
        } bindContract method

    fun handler(id: String): HttpHandler =
        Cors(
            CorsPolicy(
                originPolicy = OriginPolicy.AllowAll(),
                headers = emptyList(),
                methods = listOf(GET),
            ),
        ).then handle@{ request ->
            val nodeService =
                Services.INSTANCE.get(NuminousNodeService::class.java)
                    ?: return@handle Response(INTERNAL_SERVER_ERROR).with(
                        ErrorResponse.lens of ErrorResponse("No node service available"),
                    )
            val node =
                nodeService.getNodeById(id)
                    ?: return@handle Response(NOT_FOUND).with(
                        ErrorResponse.lens of ErrorResponse("Node not found"),
                    )
            return@handle Response(OK).with(
                NodeResponse.lens of node.toResponse(),
            )
        }

    return spec to ::handler
}
