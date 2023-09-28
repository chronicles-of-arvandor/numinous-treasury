package net.arvandor.numinoustreasury.item;

import net.md_5.bungee.api.ChatColor;

public enum NuminousRarity {
    POOR("Poor", ChatColor.of("#9d9d9d")),
    COMMON("Common", ChatColor.of("#ffffff")),
    UNCOMMON("Uncommon", ChatColor.of("#1eff00")),
    RARE("Rare", ChatColor.of("#0070dd")),
    LEGENDARY("Legendary", ChatColor.of("#ff8000")),
    UNIQUE("Unique", ChatColor.of("#e6cc80"));

    private final String displayName;
    private final ChatColor color;

    NuminousRarity(String displayName, ChatColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }
}
