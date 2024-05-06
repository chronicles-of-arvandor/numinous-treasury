package net.arvandor.numinoustreasury.item

import net.md_5.bungee.api.ChatColor

enum class NuminousRarity(val displayName: String, val color: ChatColor) {
    POOR("Poor", ChatColor.of("#9d9d9d")),
    COMMON("Common", ChatColor.of("#ffffff")),
    UNCOMMON("Uncommon", ChatColor.of("#1eff00")),
    RARE("Rare", ChatColor.of("#0070dd")),
    LEGENDARY("Legendary", ChatColor.of("#ff8000")),
    UNIQUE("Unique", ChatColor.of("#e6cc80")),
}
