package net.arvandor.numinoustreasury.listener;

import net.arvandor.numinoustreasury.item.NuminousItemInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public final class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof NuminousItemInventoryHolder holder)) {
            return;
        }
        holder.updateItemStack();
    }

}
