package net.arvandor.numinoustreasury.item;

import static java.util.Map.entry;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static org.bukkit.persistence.PersistentDataType.STRING;
import static org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY;

import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.action.NuminousOnEat;
import net.arvandor.numinoustreasury.item.action.NuminousOnInteractAir;
import net.arvandor.numinoustreasury.item.action.NuminousOnInteractBlock;
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry;
import net.arvandor.numinoustreasury.measurement.Weight;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SerializableAs("NuminousItemType")
public final class NuminousItemType implements ConfigurationSerializable, Comparable<NuminousItemType> {

    private final NuminousTreasury plugin;
    private final String id;
    private String name;
    private final List<NuminousItemCategory> categories;
    private NuminousRarity rarity;
    private ItemStack minecraftItem;
    private final List<NuminousOnEat> onEat;
    private final List<NuminousOnInteractBlock> onInteractBlock;
    private final List<NuminousOnInteractAir> onInteractAir;
    private Weight weight;
    private int inventorySlots;
    private boolean isAllowLogEntries;

    public NuminousItemType(NuminousTreasury plugin,
                            String id,
                            String name,
                            List<NuminousItemCategory> categories,
                            NuminousRarity rarity,
                            ItemStack minecraftItem,
                            List<NuminousOnEat> onEat,
                            List<NuminousOnInteractBlock> onInteractBlock,
                            List<NuminousOnInteractAir> onInteractAir,
                            Weight weight,
                            int inventorySlots,
                            boolean isAllowLogEntries) {
        this.plugin = plugin;
        this.id = id;
        this.name = name;
        this.categories = categories;
        this.rarity = rarity;
        this.minecraftItem = minecraftItem;
        this.onEat = onEat;
        this.onInteractBlock = onInteractBlock;
        this.onInteractAir = onInteractAir;
        this.weight = weight;
        this.inventorySlots = inventorySlots;
        this.isAllowLogEntries = isAllowLogEntries;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NuminousItemCategory> getCategories() {
        return categories;
    }

    public void setRarity(NuminousRarity rarity) {
        this.rarity = rarity;
    }

    public NuminousRarity getRarity() {
        return rarity;
    }

    public ItemStack getMinecraftItem() {
        return minecraftItem;
    }

    public void setMinecraftItem(ItemStack minecraftItem) {
        this.minecraftItem = minecraftItem;
    }

    public List<NuminousOnEat> getOnEat() {
        return onEat;
    }

    public List<NuminousOnInteractBlock> getOnInteractBlock() {
        return onInteractBlock;
    }

    public List<NuminousOnInteractAir> getOnInteractAir() {
        return onInteractAir;
    }

    public Weight getWeight() {
        return weight;
    }

    public void setWeight(Weight weight) {
        this.weight = weight;
    }

    public int getInventorySlots() {
        return inventorySlots;
    }

    public void setInventorySlots(int inventorySlots) {
        this.inventorySlots = inventorySlots;
    }

    public boolean isAllowLogEntries() {
        return isAllowLogEntries;
    }

    public void setAllowLogEntries(boolean allowLogEntries) {
        isAllowLogEntries = allowLogEntries;
    }

    public ItemStack toItemStack(int amount, List<NuminousLogEntry> logEntries) {
        ItemStack itemStack = new ItemStack(getMinecraftItem());
        itemStack.setAmount(amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(getMinecraftItem().getType());
        }
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.keys().itemId(), STRING, getId());
            final ItemMeta finalMeta = meta;
            if (logEntries != null) {
                meta.getPersistentDataContainer().set(
                        plugin.keys().logEntries(),
                        TAG_CONTAINER_ARRAY,
                        logEntries.stream()
                                .map(entry -> entry.toCompoundTag(plugin, finalMeta.getPersistentDataContainer()))
                                .toArray(PersistentDataContainer[]::new));
            }
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(0, getRarity().getColor() + getRarity().getDisplayName());
            lore.add(1, GRAY + "Weight: " + getWeight().toString());
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public boolean isItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        String id = meta.getPersistentDataContainer().get(plugin.keys().itemId(), STRING);
        if (id == null) return false;
        return id.equals(getId());
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.ofEntries(
                entry("id", getId()),
                entry("name", getName()),
                entry("categories", getCategories().stream().map(NuminousItemCategory::name).toList()),
                entry("rarity", getRarity().name()),
                entry("minecraft-item", getMinecraftItem()),
                entry("on-eat", getOnEat()),
                entry("on-interact-block", getOnInteractBlock()),
                entry("on-interact-air", getOnInteractAir()),
                entry("weight", getWeight()),
                entry("inventory-slots", getInventorySlots()),
                entry("allow-log-entries", isAllowLogEntries())
        );
    }

    public static NuminousItemType deserialize(Map<String, Object> serialized) {
        return new NuminousItemType(
                (NuminousTreasury) Bukkit.getPluginManager().getPlugin("numinous-treasury"),
                (String) serialized.get("id"),
                (String) serialized.get("name"),
                new ArrayList<>(((List<String>) serialized.get("categories")).stream().map(NuminousItemCategory::valueOf).toList()),
                NuminousRarity.valueOf((String) serialized.get("rarity")),
                (ItemStack) serialized.get("minecraft-item"),
                new ArrayList<>((List<NuminousOnEat>) serialized.get("on-eat")),
                new ArrayList<>((List<NuminousOnInteractBlock>) serialized.get("on-interact-block")),
                new ArrayList<>((List<NuminousOnInteractAir>) serialized.get("on-interact-air")),
                (Weight) serialized.get("weight"),
                (int) serialized.getOrDefault("inventory-slots", 0),
                serialized.containsKey("allow-log-entries") ? (boolean) serialized.get("allow-log-entries") : true
        );
    }

    @Override
    public int compareTo(NuminousItemType other) {
        return getName().compareTo(other.getName());
    }
}
