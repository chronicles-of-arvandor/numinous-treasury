package net.arvandor.numinoustreasury.item;

import static net.md_5.bungee.api.ChatColor.GRAY;
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
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SerializableAs("NuminousItemStack")
public final class NuminousItemStack implements ConfigurationSerializable {

    private final NuminousItemType itemType;
    private final int amount;
    private final ItemStack[] inventoryContents;
    private final List<NuminousLogEntry> logEntries;

    public NuminousItemStack(NuminousItemType itemType, int amount, ItemStack[] inventoryContents, List<NuminousLogEntry> logEntries) {
        this.itemType = itemType;
        this.amount = amount;
        this.inventoryContents = inventoryContents;
        this.logEntries = logEntries;
    }

    public NuminousItemStack(NuminousItemType itemType, int amount) {
        this(itemType, amount, null, List.of());
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

    public ItemStack[] getInventoryContents() {
        return inventoryContents;
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

        ItemStack[] inventoryContents = null;
        PersistentDataContainer[] itemTagContainers = meta.getPersistentDataContainer().get(plugin.keys().inventory(), TAG_CONTAINER_ARRAY);
        if (itemTagContainers != null) {
            inventoryContents = Arrays.stream(itemTagContainers)
                    .map(itemTagContainer -> itemTagContainer.get(
                            plugin.keys().inventoryItem(),
                            plugin.persistentDataTypes().itemStack()
                    )).toArray(ItemStack[]::new);
        }

        List<NuminousLogEntry> logEntries = meta.getPersistentDataContainer().has(plugin.keys().logEntries(), TAG_CONTAINER_ARRAY)
                ? Arrays.stream(meta.getPersistentDataContainer().get(plugin.keys().logEntries(), TAG_CONTAINER_ARRAY))
                .map(compoundTag -> NuminousLogEntry.fromCompoundTag(plugin, compoundTag))
                .toList()
                : List.of();
        return new NuminousItemStack(itemType, amount, inventoryContents, logEntries);
    }

    public void update(ItemStack itemStack, boolean updateLore) {
        if (itemStack == null) return;
        NuminousTreasury plugin = (NuminousTreasury) Bukkit.getPluginManager().getPlugin("numinous-treasury");

        itemStack.setType(itemType.getMinecraftItem().getType());
        itemStack.setAmount(amount);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(plugin.keys().itemId(), STRING, itemType.getId());
        meta.getPersistentDataContainer().set(plugin.keys().inventory(), TAG_CONTAINER_ARRAY, Arrays.stream(inventoryContents)
                .map(item -> {
                    PersistentDataContainer container = meta.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
                    if (item != null) {
                        container.set(plugin.keys().inventoryItem(), plugin.persistentDataTypes().itemStack(), item);
                    }
                    return container;
                }).toArray(PersistentDataContainer[]::new));
        meta.getPersistentDataContainer().set(plugin.keys().logEntries(), TAG_CONTAINER_ARRAY, logEntries.stream()
                .map(logEntry -> logEntry.toCompoundTag(plugin, meta.getPersistentDataContainer()))
                .toArray(PersistentDataContainer[]::new));

        if (updateLore) {
            ItemMeta typeMeta = itemType.getMinecraftItem().getItemMeta();
            if (typeMeta != null) {
                meta.setDisplayName(typeMeta.getDisplayName());
            }

            List<String> lore = new ArrayList<>();
            NuminousRarity rarity = itemType.getRarity();
            lore.add(0, rarity.getColor() + rarity.getDisplayName());
            lore.add(1, GRAY + "Weight: " + itemType.getWeight().toString());
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
    }

    public NuminousItemStack copy(
            NuminousItemType itemType,
            Integer amount,
            ItemStack[] inventoryContents,
            List<NuminousLogEntry> logEntries
    ) {
        return new NuminousItemStack(
                itemType == null ? this.itemType : itemType,
                amount == null ? this.amount : amount,
                inventoryContents == null ? this.inventoryContents : inventoryContents,
                logEntries == null ? this.logEntries : logEntries
        );
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "item-type", getItemType().getId(),
                "amount", getAmount(),
                "inventory-contents", getInventoryContents(),
                "log-entries", getLogEntries()
        );
    }

    public static NuminousItemStack deserialize(Map<String, Object> serialized) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        List<ItemStack> inventory = (List<ItemStack>) serialized.get("inventory-contents");
        return new NuminousItemStack(
                itemService.getItemTypeById((String) serialized.get("item-type")),
                (Integer) serialized.get("amount"),
                inventory == null ? null : inventory.toArray(ItemStack[]::new),
                (List<NuminousLogEntry>) serialized.get("log-entries")
        );
    }
}
