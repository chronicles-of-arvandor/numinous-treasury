package net.arvandor.numinoustreasury.listener

import net.arvandor.numinoustreasury.item.NuminousItemInventoryHolder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventory = event.inventory
        val holder = inventory.holder
        if (holder !is NuminousItemInventoryHolder) {
            return
        }
        holder.updateItemStack()
    }
}
