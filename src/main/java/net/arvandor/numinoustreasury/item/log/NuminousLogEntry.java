package net.arvandor.numinoustreasury.item.log;

import static org.bukkit.persistence.PersistentDataType.BOOLEAN;
import static org.bukkit.persistence.PersistentDataType.STRING;

import net.arvandor.numinoustreasury.NuminousTreasury;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.persistence.PersistentDataContainer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@SerializableAs("NuminousLogEntry")
public final class NuminousLogEntry implements ConfigurationSerializable {

    private final Instant createdAt;
    private final UUID minecraftUuid;
    private final boolean isSystem;
    private final BaseComponent[] text;

    public NuminousLogEntry(Instant createdAt, UUID minecraftUuid, boolean isSystem, BaseComponent[] text) {
        this.createdAt = createdAt;
        this.minecraftUuid = minecraftUuid;
        this.isSystem = isSystem;
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public BaseComponent[] getText() {
        return text;
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "created-at", DateTimeFormatter.ISO_INSTANT.format(createdAt),
                "minecraft-uuid", minecraftUuid.toString(),
                "is-system", isSystem,
                "text", ComponentSerializer.toString(text)
        );
    }

    public static NuminousLogEntry deserialize(Map<String, Object> serialized) {
        return new NuminousLogEntry(
                Instant.parse((String) serialized.get("created-at")),
                UUID.fromString((String) serialized.get("minecraft-uuid")),
                (Boolean) serialized.get("is-system"),
                ComponentSerializer.parse((String) serialized.get("text"))
        );
    }

    public PersistentDataContainer toCompoundTag(NuminousTreasury plugin, PersistentDataContainer parent) {
        PersistentDataContainer compoundTag = parent.getAdapterContext().newPersistentDataContainer();
        compoundTag.set(plugin.keys().logEntryCreatedAt(), STRING, DateTimeFormatter.ISO_INSTANT.format(createdAt));
        compoundTag.set(plugin.keys().logEntryMinecraftUuid(), STRING, minecraftUuid.toString());
        compoundTag.set(plugin.keys().logEntryIsSystem(), BOOLEAN, isSystem);
        compoundTag.set(plugin.keys().logEntryText(), STRING, ComponentSerializer.toString(text));
        return compoundTag;
    }

    public static NuminousLogEntry fromCompoundTag(NuminousTreasury plugin, PersistentDataContainer compoundTag) {
        return new NuminousLogEntry(
                Instant.parse(compoundTag.get(plugin.keys().logEntryCreatedAt(), STRING)),
                UUID.fromString(compoundTag.get(plugin.keys().logEntryMinecraftUuid(), STRING)),
                compoundTag.get(plugin.keys().logEntryIsSystem(), BOOLEAN),
                ComponentSerializer.parse(compoundTag.get(plugin.keys().logEntryText(), STRING))
        );
    }
}
