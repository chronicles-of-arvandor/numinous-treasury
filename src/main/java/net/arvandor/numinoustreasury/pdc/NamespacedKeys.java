package net.arvandor.numinoustreasury.pdc;

import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.NamespacedKey;

public final class NamespacedKeys {

    private final NamespacedKey itemId;
    private final NamespacedKey logEntries;
    private final NamespacedKey logEntryCreatedAt;
    private final NamespacedKey logEntryMinecraftUuid;
    private final NamespacedKey logEntryIsSystem;
    private final NamespacedKey logEntryText;
    private final NamespacedKey inventory;
    private final NamespacedKey inventoryItem;

    public NamespacedKeys(NuminousTreasury plugin) {
        this.itemId = new NamespacedKey(plugin, "id");
        this.logEntries = new NamespacedKey(plugin, "logEntries");
        this.logEntryCreatedAt = new NamespacedKey(plugin, "createdAt");
        this.logEntryMinecraftUuid = new NamespacedKey(plugin, "minecraftUuid");
        this.logEntryIsSystem = new NamespacedKey(plugin, "isSystem");
        this.logEntryText = new NamespacedKey(plugin, "text");
        this.inventory = new NamespacedKey(plugin, "inventory");
        this.inventoryItem = new NamespacedKey(plugin, "item");
    }

    public NamespacedKey itemId() {
        return itemId;
    }

    public NamespacedKey logEntries() {
        return logEntries;
    }

    public NamespacedKey logEntryCreatedAt() {
        return logEntryCreatedAt;
    }

    public NamespacedKey logEntryMinecraftUuid() {
        return logEntryMinecraftUuid;
    }

    public NamespacedKey logEntryIsSystem() {
        return logEntryIsSystem;
    }

    public NamespacedKey logEntryText() {
        return logEntryText;
    }

    public NamespacedKey inventory() {
        return inventory;
    }

    public NamespacedKey inventoryItem() {
        return inventoryItem;
    }
}
