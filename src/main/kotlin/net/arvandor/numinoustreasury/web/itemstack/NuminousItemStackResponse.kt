package net.arvandor.numinoustreasury.web.itemstack

import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.arvandor.numinoustreasury.web.item.ItemResponse
import net.arvandor.numinoustreasury.web.item.toResponse

data class NuminousItemStackResponse(
    val itemType: ItemResponse,
    val amount: Int,
)

fun NuminousItemStack.toResponse() =
    NuminousItemStackResponse(
        itemType = itemType.toResponse(),
        amount = amount,
    )
