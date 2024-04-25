package net.arvandor.numinoustreasury.pdc

import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.NamespacedKey

class NamespacedKeys(plugin: NuminousTreasury) {
    val itemId = NamespacedKey(plugin, "id")
    val logEntries = NamespacedKey(plugin, "logEntries")
    val logEntryCreatedAt = NamespacedKey(plugin, "createdAt")
    val logEntryMinecraftUuid = NamespacedKey(plugin, "minecraftUuid")
    val logEntryIsSystem = NamespacedKey(plugin, "isSystem")
    val logEntryText = NamespacedKey(plugin, "text")
    val inventory = NamespacedKey(plugin, "inventory")
    val inventoryItem = NamespacedKey(plugin, "item")
}
