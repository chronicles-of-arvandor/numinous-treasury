package net.arvandor.numinoustreasury.item.log

import net.arvandor.numinoustreasury.NuminousTreasury
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

@SerializableAs("NuminousLogEntry")
class NuminousLogEntry(
    val createdAt: Instant,
    val minecraftUuid: UUID?,
    val isSystem: Boolean,
    val text: Array<BaseComponent>,
) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> {
        return mapOf(
            "created-at" to DateTimeFormatter.ISO_INSTANT.format(createdAt),
            "minecraft-uuid" to minecraftUuid.toString(),
            "is-system" to isSystem,
            "text" to ComponentSerializer.toString(*text),
        )
    }

    fun toCompoundTag(
        plugin: NuminousTreasury,
        parent: PersistentDataContainer,
    ): PersistentDataContainer {
        val compoundTag = parent.adapterContext.newPersistentDataContainer()
        compoundTag.set(
            plugin.keys.logEntryCreatedAt,
            PersistentDataType.STRING,
            DateTimeFormatter.ISO_INSTANT.format(
                createdAt,
            ),
        )
        compoundTag.set(plugin.keys.logEntryMinecraftUuid, PersistentDataType.STRING, minecraftUuid.toString())
        compoundTag.set(plugin.keys.logEntryIsSystem, PersistentDataType.BOOLEAN, isSystem)
        compoundTag.set(plugin.keys.logEntryText, PersistentDataType.STRING, ComponentSerializer.toString(*text))
        return compoundTag
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): NuminousLogEntry {
            return NuminousLogEntry(
                Instant.parse(serialized["created-at"] as String),
                UUID.fromString(serialized["minecraft-uuid"] as String),
                (serialized["is-system"] as Boolean),
                ComponentSerializer.parse(serialized["text"] as String),
            )
        }

        fun fromCompoundTag(
            plugin: NuminousTreasury,
            compoundTag: PersistentDataContainer,
        ): NuminousLogEntry {
            return NuminousLogEntry(
                Instant.parse(compoundTag.get(plugin.keys.logEntryCreatedAt, PersistentDataType.STRING)),
                UUID.fromString(compoundTag.get(plugin.keys.logEntryMinecraftUuid, PersistentDataType.STRING)),
                compoundTag.get(plugin.keys.logEntryIsSystem, PersistentDataType.BOOLEAN) ?: false,
                ComponentSerializer.parse(compoundTag.get(plugin.keys.logEntryText, PersistentDataType.STRING)),
            )
        }
    }
}
