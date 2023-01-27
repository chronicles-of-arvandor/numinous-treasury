package net.kingdommc.darkages.numinoustreasury;

import org.bukkit.NamespacedKey;

public final class NamespacedKeys {

    private final NamespacedKey itemId;

    public NamespacedKeys(NuminousTreasury plugin) {
        this.itemId = new NamespacedKey(plugin, "id");
    }

    public NamespacedKey itemId() {
        return itemId;
    }
}
