package net.arvandor.numinoustreasury.item

import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.action.NuminousOnEat
import net.arvandor.numinoustreasury.item.action.NuminousOnInteractAir
import net.arvandor.numinoustreasury.item.action.NuminousOnInteractBlock
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.arvandor.numinoustreasury.measurement.Weight
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

@SerializableAs("NuminousItemType")
class NuminousItemType(
    private val plugin: NuminousTreasury,
    val id: String,
    var name: String,
    val categories: MutableList<NuminousItemCategory>,
    var rarity: NuminousRarity,
    var minecraftItem: ItemStack,
    val onEat: List<NuminousOnEat>,
    val onInteractBlock: List<NuminousOnInteractBlock>,
    val onInteractAir: List<NuminousOnInteractAir>,
    var weight: Weight,
    var inventorySlots: Int,
    var isAllowLogEntries: Boolean,
) : ConfigurationSerializable, Comparable<NuminousItemType> {
    fun toItemStack(
        amount: Int,
        logEntries: List<NuminousLogEntry>?,
    ): ItemStack {
        val itemStack = ItemStack(minecraftItem)
        itemStack.amount = amount
        var meta = itemStack.itemMeta
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(minecraftItem.type)
        }
        if (meta != null) {
            meta.persistentDataContainer.set(plugin.keys.itemId, PersistentDataType.STRING, id)
            val finalMeta: ItemMeta = meta
            if (logEntries != null) {
                meta.persistentDataContainer.set<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                    plugin.keys.logEntries,
                    PersistentDataType.TAG_CONTAINER_ARRAY,
                    logEntries
                        .map { entry ->
                            entry.toCompoundTag(
                                plugin,
                                finalMeta.persistentDataContainer,
                            )
                        }
                        .toTypedArray(),
                )
            }
            var lore = meta.lore
            if (lore == null) {
                lore = ArrayList()
            }
            lore.add(0, rarity.color.toString() + rarity.displayName)
            lore.add(1, ChatColor.GRAY.toString() + "Weight: " + weight.toString())
            meta.lore = lore
        }
        itemStack.setItemMeta(meta)
        return itemStack
    }

    fun isItem(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val meta = itemStack.itemMeta ?: return false
        val id =
            meta.persistentDataContainer.get(
                plugin.keys.itemId,
                PersistentDataType.STRING,
            )
                ?: return false
        return id == this.id
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "categories" to categories.map { it.name },
            "rarity" to rarity.name,
            "minecraft-item" to minecraftItem,
            "on-eat" to onEat,
            "on-interact-block" to onInteractBlock,
            "on-interact-air" to onInteractAir,
            "weight" to weight,
            "inventory-slots" to inventorySlots,
            "allow-log-entries" to isAllowLogEntries,
        )
    }

    override fun compareTo(other: NuminousItemType): Int {
        return name.compareTo(other.name)
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any>): NuminousItemType {
            return NuminousItemType(
                Bukkit.getPluginManager().getPlugin("numinous-treasury") as NuminousTreasury,
                serialized["id"] as String,
                serialized["name"] as String,
                ArrayList(
                    (serialized["categories"] as List<String>)
                        .map(NuminousItemCategory::valueOf),
                ),
                NuminousRarity.valueOf(serialized["rarity"] as String),
                serialized["minecraft-item"] as ItemStack,
                ArrayList(serialized["on-eat"] as List<NuminousOnEat>),
                ArrayList(serialized["on-interact-block"] as List<NuminousOnInteractBlock>),
                ArrayList(serialized["on-interact-air"] as List<NuminousOnInteractAir>),
                serialized["weight"] as Weight,
                serialized.getOrDefault("inventory-slots", 0) as Int,
                if (serialized.containsKey("allow-log-entries")) serialized["allow-log-entries"] as Boolean else true,
            )
        }
    }
}
