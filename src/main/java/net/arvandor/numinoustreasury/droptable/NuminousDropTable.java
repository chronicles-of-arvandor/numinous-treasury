package net.arvandor.numinoustreasury.droptable;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SerializableAs("NuminousDropTable")
public final class NuminousDropTable implements ConfigurationSerializable, Comparable<NuminousDropTable> {

    private final String id;
    private final List<NuminousDropTableItem> items;
    private final Random random;

    public NuminousDropTable(String id, List<NuminousDropTableItem> items) {
        this.id = id;
        this.items = items;
        this.random = new Random();
    }

    public String getId() {
        return id;
    }

    public List<NuminousDropTableItem> getItems() {
        return items;
    }

    public NuminousDropTableItem chooseItem() {
        int chanceSum = getItems().stream().map(NuminousDropTableItem::getChance).reduce(0, Integer::sum);
        int choice = random.nextInt(chanceSum);
        int sum = 0;
        for (NuminousDropTableItem item : getItems()) {
            sum += item.getChance();
            if (sum > choice) return item;
        }
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "id", getId(),
                "items", getItems()
        );
    }

    public static NuminousDropTable deserialize(Map<String, Object> serialized) {
        return new NuminousDropTable(
                (String) serialized.get("id"),
                (List<NuminousDropTableItem>) serialized.get("items")
        );
    }

    @Override
    public int compareTo(NuminousDropTable other) {
        return getId().compareTo(other.getId());
    }
}
