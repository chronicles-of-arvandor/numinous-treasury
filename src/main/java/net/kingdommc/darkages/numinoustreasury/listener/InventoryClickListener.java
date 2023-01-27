package net.kingdommc.darkages.numinoustreasury.listener;

import net.kingdommc.darkages.numinoustreasury.workstation.WorkstationInterface;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof WorkstationInterface workstationInterface)) return;
        event.setCancelled(true);
        workstationInterface.onClick(event.getSlot());
    }

}
