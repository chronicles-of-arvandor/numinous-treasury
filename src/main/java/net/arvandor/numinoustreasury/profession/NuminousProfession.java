package net.arvandor.numinoustreasury.profession;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("NuminousProfession")
public final class NuminousProfession implements ConfigurationSerializable {

    private final String id;
    private final String name;

    public NuminousProfession(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "id", getId(),
                "name", getName()
        );
    }

    public static NuminousProfession deserialize(Map<String, Object> serialized) {
        return new NuminousProfession(
                (String) serialized.get("id"),
                (String) serialized.get("name")
        );
    }
}
