package net.arvandor.numinoustreasury.web.node

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.web.error.ErrorResponse
import net.arvandor.numinoustreasury.web.stamina.StaminaCostResponse
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

fun nodesRoute(method: Method): ContractRoute {
    val nameQuery = Query.string().optional("name", "All or part of the name of the node")
    val professionQuery = Query.string().optional("profession", "The ID of a profession")
    val levelGteQuery = Query.int().optional("levelGte", "Filter levels greater than or equal to n, ignored if no profession specified")
    val levelLteQuery = Query.int().optional("levelLte", "Filter levels less than or equal to n, ignored if no profession specified")
    val levelExactQuery = Query.int().optional("level", "Nodes that require exactly level n, ignored if no profession specified")
    val experienceGteQuery = Query.int().optional("experienceGte", "Filter nodes with experience greater than or equal to n")
    val experienceLteQuery = Query.int().optional("experienceLte", "Filter nodes with experience less than or equal to n")
    val staminaCostQuery = Query.enum<StaminaCostResponse>().optional("staminaCost", "Filter nodes by stamina cost")
    val dropTableQuery = Query.string().optional("dropTable", "The ID of a drop table")
    val spec =
        "/nodes" meta {
            summary = "Get a list of nodes"
            operationId = "listNodes"
            queries += nameQuery
            queries += professionQuery
            queries += levelGteQuery
            queries += levelLteQuery
            queries += levelExactQuery
            queries += experienceGteQuery
            queries += experienceLteQuery
            queries += staminaCostQuery
            queries += dropTableQuery
            returning(
                OK,
                NodeResponse.listLens to
                    listOf(
                        NodeResponse(
                            "mining_1",
                            "Mining Node 1",
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
                    ),
            )
        } bindContract method

    fun handler(request: Request): Response {
        val name = nameQuery(request)
        val profession = professionQuery(request)
        val levelGte = levelGteQuery(request)
        val levelLte = levelLteQuery(request)
        val levelExact = levelExactQuery(request)
        val experienceGte = experienceGteQuery(request)
        val experienceLte = experienceLteQuery(request)
        val staminaCost = staminaCostQuery(request)
        val dropTable = dropTableQuery(request)

        val nodeService =
            Services.INSTANCE.get(NuminousNodeService::class.java)
                ?: return Response(INTERNAL_SERVER_ERROR).with(
                    ErrorResponse.lens of ErrorResponse("No node service available"),
                )
        val filter = fun(node: NodeResponse): Boolean {
            if (name != null && !node.name.contains(name, ignoreCase = true)) {
                return false
            }
            if (profession != null) {
                val requiredProfessionLevel = node.requiredProfessionLevels.singleOrNull { it.id == profession }
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
            if (experienceGte != null && node.experience < experienceGte) {
                return false
            }
            if (experienceLte != null && node.experience > experienceLte) {
                return false
            }
            if (staminaCost != null && node.staminaCost != staminaCost) {
                return false
            }
            if (dropTable != null && node.dropTableId != dropTable) {
                return false
            }
            return true
        }
        val nodes = nodeService.nodes.map { it.toResponse() }.filter(filter)
        return Response(OK).with(NodeResponse.listLens of nodes)
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
