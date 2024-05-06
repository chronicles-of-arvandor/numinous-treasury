package net.arvandor.numinoustreasury.web.droptable

import net.arvandor.numinoustreasury.droptable.NuminousDropTable
import net.arvandor.numinoustreasury.droptable.NuminousDropTableItem
import net.arvandor.numinoustreasury.web.itemstack.NuminousItemStackResponse
import net.arvandor.numinoustreasury.web.itemstack.toResponse
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class DropTableResponse(
    val id: String,
    val items: List<DropTableItemResponse>,
) {
    companion object {
        val lens = Body.auto<DropTableResponse>().toLens()
        val listLens = Body.auto<List<DropTableResponse>>().toLens()
    }
}

fun NuminousDropTable.toResponse() =
    DropTableResponse(
        id = id,
        items = items.toResponse(),
    )

data class DropTableItemResponse(
    val items: List<NuminousItemStackResponse>,
    val chance: Double,
)

fun List<NuminousDropTableItem>.toResponse(): List<DropTableItemResponse> {
    val chanceSum = sumOf { it.chance }
    return map { it.toResponse().copy(chance = (it.chance / chanceSum) * 100) }
}

fun NuminousDropTableItem.toResponse() =
    DropTableItemResponse(
        items = items.map { it.toResponse() },
        chance = chance,
    )
