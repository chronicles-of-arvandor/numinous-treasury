package net.arvandor.numinoustreasury.listener;

import net.arvandor.numinoustreasury.workstation.WorkstationInterface;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public final class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof WorkstationInterface workstationInterface)) return;
        event.setCancelled(true);
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.getHolder() instanceof WorkstationInterface) {
            workstationInterface.onClick(event.getSlot());
        }
    }

}
