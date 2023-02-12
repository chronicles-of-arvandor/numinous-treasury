package net.kingdommc.darkages.numinoustreasury.item;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

import static org.bukkit.persistence.PersistentDataType.STRING;

@SerializableAs("NuminousItemStack")
public final class NuminousItemStack implements ConfigurationSerializable {

    private final NuminousItemType itemType;
    private final int amount;

    public NuminousItemStack(NuminousItemType itemType, int amount) {
        this.itemType = itemType;
        this.amount = amount;
    }

    public NuminousItemStack(NuminousItemType itemType) {
        this(itemType, 1);
    }

    public NuminousItemType getItemType() {
        return itemType;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack toItemStack() {
        return itemType.toItemStack(amount);
    }

    public static NuminousItemStack fromItemStack(ItemStack itemStack) {
        if (itemStack == null) return null;
        NuminousTreasury plugin = (NuminousTreasury) Bukkit.getPluginManager().getPlugin("numinous-treasury");
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        String itemId = meta.getPersistentDataContainer().get(plugin.keys().itemId(), STRING);
        if (itemId == null) return null;
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        NuminousItemType itemType = itemService.getItemTypeById(itemId);
        if (itemType == null) return null;
        int amount = itemStack.getAmount();
        return new NuminousItemStack(itemType, amount);
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "item-type", getItemType().getId(),
                "amount", getAmount()
        );
    }

    public static NuminousItemStack deserialize(Map<String, Object> serialized) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        return new NuminousItemStack(
                itemService.getItemTypeById((String) serialized.get("item-type")),
                (Integer) serialized.get("amount")
        );
    }
}
