package net.arvandor.numinoustreasury.web.node

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.node.NuminousNode
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.web.stamina.StaminaCostResponse
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class NodeResponse(
    val id: String,
    val name: String,
    val requiredProfessionLevels: List<RequiredProfessionResponse>,
    val experience: Int,
    val staminaCost: StaminaCostResponse,
    val dropTableId: String,
) {
    companion object {
        val lens = Body.auto<NodeResponse>().toLens()
        val listLens = Body.auto<List<NodeResponse>>().toLens()
    }
}

fun NuminousNode.toResponse(): NodeResponse {
    val nodeService =
        Services.INSTANCE.get(NuminousNodeService::class.java)
            ?: throw IllegalStateException("No NuminousNodeService found")
    val nodes = nodeService.nodes
    val staminaCostLowerQuartile = nodes.map { it.staminaCost }.sorted()[nodes.size / 4]
    val staminaCostUpperQuartile = nodes.map { it.staminaCost }.sorted()[nodes.size * 3 / 4]
    val staminaCostLevel =
        when {
            staminaCost < staminaCostLowerQuartile -> StaminaCostResponse.LOW
            staminaCost < staminaCostUpperQuartile -> StaminaCostResponse.MEDIUM
            else -> StaminaCostResponse.HIGH
        }
    return NodeResponse(
        id = id,
        name = name,
        requiredProfessionLevels =
            requiredProfessionLevel.map { (profession, level) ->
                RequiredProfessionResponse(
                    id = profession.id,
                    name = profession.name,
                    level = level,
                )
            },
        experience = experience,
        staminaCost = staminaCostLevel,
        dropTableId = dropTable.id,
    )
}

data class RequiredProfessionResponse(
    val id: String,
    val name: String,
    val level: Int,
)
