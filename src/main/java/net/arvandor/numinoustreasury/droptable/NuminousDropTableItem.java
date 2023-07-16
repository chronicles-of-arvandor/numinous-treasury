package net.arvandor.numinoustreasury.droptable;

import net.arvandor.numinoustreasury.item.NuminousItemStack;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.List;
import java.util.Map;

@SerializableAs("NuminousDropTableItem")
public final class NuminousDropTableItem implements ConfigurationSerializable, Comparable<NuminousDropTableItem> {

    private final List<NuminousItemStack> items;
    private final int chance;

    public NuminousDropTableItem(List<NuminousItemStack> items, int chance) {
        this.items = items;
        this.chance = chance;
    }

    public List<NuminousItemStack> getItems() {
        return items;
    }

    public int getChance() {
        return chance;
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "items", getItems(),
                "chance", getChance()
        );
    }

    public static NuminousDropTableItem deserialize(Map<String, Object> serialized) {
        return new NuminousDropTableItem(
                (List<NuminousItemStack>) serialized.get("items"),
                (int) serialized.get("chance")
        );
    }

    @Override
    public int compareTo(NuminousDropTableItem other) {
        return other.getChance() - getChance();
    }
}
