package net.arvandor.numinoustreasury.item;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class NuminousItemInventoryHolder implements InventoryHolder {

    private final NuminousItemStack numinousItemStack;
    private final ItemStack itemStack;
    private final Inventory inventory;

    public NuminousItemInventoryHolder(NuminousItemStack numinousItemStack, ItemStack itemStack) {
        this.numinousItemStack = numinousItemStack;
        this.itemStack = itemStack;
        this.inventory = Bukkit.createInventory(this, numinousItemStack.getItemType().getInventorySlots(), numinousItemStack.getItemType().getName());
        this.inventory.setContents(numinousItemStack.getInventoryContents());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void updateItemStack() {
        NuminousItemStack newNuminousItemStack = numinousItemStack.copy(
                null,
                null,
                inventory.getContents(),
                null
        );

        newNuminousItemStack.update(itemStack, false);
    }
}
