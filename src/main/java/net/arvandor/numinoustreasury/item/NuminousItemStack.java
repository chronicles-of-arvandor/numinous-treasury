package net.arvandor.numinoustreasury.item;

import static org.bukkit.persistence.PersistentDataType.STRING;
import static org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SerializableAs("NuminousItemStack")
public final class NuminousItemStack implements ConfigurationSerializable {

    private final NuminousItemType itemType;
    private final int amount;
    private final List<NuminousLogEntry> logEntries;

    public NuminousItemStack(NuminousItemType itemType, int amount, List<NuminousLogEntry> logEntries) {
        this.itemType = itemType;
        this.amount = amount;
        this.logEntries = logEntries;
    }

    public NuminousItemStack(NuminousItemType itemType, int amount) {
        this(itemType, amount, List.of());
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

    public List<NuminousLogEntry> getLogEntries() {
        return logEntries;
    }

    public ItemStack toItemStack() {
        return itemType.toItemStack(amount, logEntries);
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

        List<NuminousLogEntry> logEntries = meta.getPersistentDataContainer().has(plugin.keys().logEntries(), TAG_CONTAINER_ARRAY)
                ? Arrays.stream(meta.getPersistentDataContainer().get(plugin.keys().logEntries(), TAG_CONTAINER_ARRAY))
                .map(compoundTag -> NuminousLogEntry.fromCompoundTag(plugin, compoundTag))
                .toList()
                : List.of();
        return new NuminousItemStack(itemType, amount, logEntries);
    }

    public NuminousItemStack copy(
            NuminousItemType itemType,
            Integer amount,
            List<NuminousLogEntry> logEntries
    ) {
        return new NuminousItemStack(
                itemType == null ? this.itemType : itemType,
                amount == null ? this.amount : amount,
                logEntries == null ? this.logEntries : logEntries
        );
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "item-type", getItemType().getId(),
                "amount", getAmount(),
                "log-entries", getLogEntries()
        );
    }

    public static NuminousItemStack deserialize(Map<String, Object> serialized) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        return new NuminousItemStack(
                itemService.getItemTypeById((String) serialized.get("item-type")),
                (Integer) serialized.get("amount"),
                (List<NuminousLogEntry>) serialized.get("log-entries")
        );
    }
}
