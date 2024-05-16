package net.arvandor.numinoustreasury.web.item

import net.arvandor.numinoustreasury.item.NuminousItemCategory
import net.arvandor.numinoustreasury.item.NuminousItemType
import net.arvandor.numinoustreasury.item.NuminousRarity
import org.bukkit.Material
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class ItemResponse(
    val id: String,
    val name: String,
    val lore: List<String>,
    val categories: List<NuminousItemCategory>,
    val rarity: NuminousRarity,
    val weight: String,
    val minecraftItem: Material,
    val inventorySlots: Int?,
    val isAllowLogEntries: Boolean,
) {
    companion object {
        val lens = Body.auto<ItemResponse>().toLens()
        val listLens = Body.auto<List<ItemResponse>>().toLens()
    }
}

fun NuminousItemType.toResponse() =
    ItemResponse(
        id = id,
        name = name,
        lore = minecraftItem.itemMeta?.lore ?: emptyList(),
        categories = categories,
        rarity = rarity,
        weight = weight.toString(),
        minecraftItem = minecraftItem.type,
        inventorySlots = inventorySlots,
        isAllowLogEntries = isAllowLogEntries,
    )
