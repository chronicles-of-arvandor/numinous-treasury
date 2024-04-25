package net.arvandor.numinoustreasury.item.action

import net.arvandor.numinoustreasury.item.NuminousItemInventoryHolder
import net.arvandor.numinoustreasury.item.NuminousItemStack
import org.bukkit.event.player.PlayerInteractEvent

class OpenInventory : NuminousOnInteractAir, NuminousOnInteractBlock {
    override fun onInteractAir(event: PlayerInteractEvent) {
        openInventory(event)
    }

    override fun onInteractBlock(event: PlayerInteractEvent) {
        openInventory(event)
    }

    override fun serialize(): Map<String, Any> {
        return mapOf()
    }

    companion object {
        private fun openInventory(event: PlayerInteractEvent) {
            val bukkitItem = event.item
            val numinousItem: NuminousItemStack = NuminousItemStack.Companion.fromItemStack(bukkitItem) ?: return
            val holder = NuminousItemInventoryHolder(numinousItem, bukkitItem)
            event.player.openInventory(holder.inventory)
        }

        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>?): OpenInventory {
            return OpenInventory()
        }
    }
}
