package net.arvandor.numinoustreasury.item

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.Contract

@SerializableAs("NuminousItemStack")
class NuminousItemStack
    @JvmOverloads
    constructor(
        val itemType: NuminousItemType,
        val amount: Int = 1,
        val inventoryContents: Array<ItemStack?>? = null,
        logEntries: List<NuminousLogEntry>? = listOf(),
    ) : ConfigurationSerializable {
        val logEntries: List<NuminousLogEntry>? = if (itemType.isAllowLogEntries) logEntries else null

        fun toItemStack(): ItemStack {
            return itemType.toItemStack(amount, logEntries)
        }

        fun update(
            itemStack: ItemStack?,
            updateLore: Boolean,
        ) {
            if (itemStack == null) return
            val plugin = Bukkit.getPluginManager().getPlugin("numinous-treasury") as NuminousTreasury

            itemStack.type = itemType.minecraftItem.type
            itemStack.amount = amount

            val meta = itemStack.itemMeta ?: return
            meta.persistentDataContainer.set(plugin.keys.itemId, PersistentDataType.STRING, itemType.id)
            meta.persistentDataContainer.set<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                plugin.keys.inventory,
                PersistentDataType.TAG_CONTAINER_ARRAY,
                inventoryContents
                    ?.map { item: ItemStack? ->
                        val container = meta.persistentDataContainer.adapterContext.newPersistentDataContainer()
                        if (item != null) {
                            container.set(
                                plugin.keys.inventoryItem,
                                plugin.persistentDataTypes.itemStack,
                                item,
                            )
                        }
                        container
                    }
                    ?.toTypedArray()
                    ?: arrayOf(),
            )
            if (logEntries != null) {
                meta.persistentDataContainer.set<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                    plugin.keys.logEntries,
                    PersistentDataType.TAG_CONTAINER_ARRAY,
                    logEntries
                        .map { logEntry ->
                            logEntry.toCompoundTag(
                                plugin,
                                meta.persistentDataContainer,
                            )
                        }
                        .toTypedArray(),
                )
            }

            if (updateLore) {
                val typeMeta = itemType.minecraftItem.itemMeta
                if (typeMeta != null) {
                    meta.setDisplayName(typeMeta.displayName)
                }

                val rarity = itemType.rarity
                meta.lore =
                    listOf(
                        rarity.color.toString() + rarity.displayName,
                        ChatColor.GRAY.toString() + "Weight: " + itemType.weight.toString(),
                    )
            }

            itemStack.setItemMeta(meta)
        }

        fun copy(
            itemType: NuminousItemType?,
            amount: Int?,
            inventoryContents: Array<ItemStack?>?,
            logEntries: List<NuminousLogEntry>?,
        ): NuminousItemStack {
            return NuminousItemStack(
                itemType ?: this.itemType,
                amount ?: this.amount,
                inventoryContents ?: this.inventoryContents,
                logEntries ?: this.logEntries,
            )
        }

        override fun serialize(): Map<String, Any?> {
            return mapOf(
                "item-type" to itemType.id,
                "amount" to amount,
                "inventory-contents" to inventoryContents,
                "log-entries" to logEntries,
            )
        }

        companion object {
            @Contract("null -> null")
            fun fromItemStack(itemStack: ItemStack?): NuminousItemStack? {
                if (itemStack == null) return null
                val plugin = Bukkit.getPluginManager().getPlugin("numinous-treasury") as NuminousTreasury
                val meta = itemStack.itemMeta ?: return null
                val itemId =
                    meta.persistentDataContainer.get(
                        plugin.keys.itemId,
                        PersistentDataType.STRING,
                    )
                if (itemId == null) return null
                val itemService =
                    Services.INSTANCE.get(
                        NuminousItemService::class.java,
                    )
                val itemType = itemService.getItemTypeById(itemId) ?: return null
                val amount = itemStack.amount

                var inventoryContents: Array<ItemStack?>? = null
                val itemTagContainers =
                    meta.persistentDataContainer.get(
                        plugin.keys.inventory,
                        PersistentDataType.TAG_CONTAINER_ARRAY,
                    )
                if (itemTagContainers != null) {
                    inventoryContents =
                        itemTagContainers
                            .map { itemTagContainer ->
                                itemTagContainer.get(
                                    plugin.keys.inventoryItem,
                                    plugin.persistentDataTypes.itemStack,
                                )
                            }.toTypedArray()
                }

                val logEntries =
                    if (meta.persistentDataContainer.has<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                            plugin.keys.logEntries,
                            PersistentDataType.TAG_CONTAINER_ARRAY,
                        )
                    ) {
                        meta.persistentDataContainer.get<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                            plugin.keys.logEntries,
                            PersistentDataType.TAG_CONTAINER_ARRAY,
                        )
                            ?.map { compoundTag -> NuminousLogEntry.fromCompoundTag(plugin, compoundTag) }
                    } else {
                        listOf()
                    }
                return NuminousItemStack(itemType, amount, inventoryContents, logEntries)
            }

            @JvmStatic
            fun deserialize(serialized: Map<String, Any?>): NuminousItemStack {
                val itemService =
                    Services.INSTANCE.get(
                        NuminousItemService::class.java,
                    )
                val inventory = serialized["inventory-contents"] as List<ItemStack?>?
                val itemType =
                    itemService.getItemTypeById(serialized["item-type"] as String)
                        ?: throw IllegalStateException("Item type " + serialized["item-type"] + " does not exist")
                return NuminousItemStack(
                    itemType,
                    (serialized["amount"] as Number).toInt(),
                    inventory?.toTypedArray(),
                    serialized["log-entries"] as List<NuminousLogEntry>?,
                )
            }
        }
    }
