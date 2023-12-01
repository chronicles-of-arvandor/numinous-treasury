package net.arvandor.numinoustreasury.item.action;

import net.arvandor.numinoustreasury.item.NuminousItemInventoryHolder;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class OpenInventory implements NuminousOnInteractAir, NuminousOnInteractBlock {

    @Override
    public void onInteractAir(PlayerInteractEvent event) {
        openInventory(event);
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        openInventory(event);
    }

    private static void openInventory(PlayerInteractEvent event) {
        ItemStack bukkitItem = event.getItem();
        NuminousItemStack numinousItem = NuminousItemStack.fromItemStack(bukkitItem);
        if (numinousItem == null) return;
        NuminousItemInventoryHolder holder = new NuminousItemInventoryHolder(numinousItem, bukkitItem);
        event.getPlayer().openInventory(holder.getInventory());
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of();
    }

    public static OpenInventory deserialize(Map<String, Object> args) {
        return new OpenInventory();
    }
}
