package net.arvandor.numinoustreasury.item

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class NuminousItemInventoryHolder(private val numinousItemStack: NuminousItemStack, private val itemStack: ItemStack?) :
    InventoryHolder {
    private val inventory =
        Bukkit.createInventory(this, numinousItemStack.itemType.inventorySlots, numinousItemStack.itemType.name)

    init {
        val inventoryContents = numinousItemStack.inventoryContents
        if (inventoryContents != null) {
            inventory.contents = inventoryContents
        }
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    fun updateItemStack() {
        val newNuminousItemStack =
            numinousItemStack.copy(
                null,
                null,
                inventory.contents,
                null,
            )

        newNuminousItemStack.update(itemStack, false)
    }
}
