package net.arvandor.numinoustreasury.listener

import net.arvandor.numinoustreasury.workstation.WorkstationInterface
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.topInventory.holder !is WorkstationInterface) return
        event.isCancelled = true
        val clickedInventory = event.clickedInventory
        val inventoryHolder = clickedInventory?.holder
        if (inventoryHolder is WorkstationInterface) {
            inventoryHolder.onClick(event.slot)
        }
    }
}
