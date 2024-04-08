package net.arvandor.numinoustreasury.item;

import com.rpkit.core.service.Service;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class NuminousItemService implements Service {

    private final NuminousTreasury plugin;
    private final File itemFolder;
    private final Map<String, NuminousItemType> itemTypesById = new HashMap<>();
    private final Map<String, NuminousItemType> itemTypesByName = new HashMap<>();

    public NuminousItemService(NuminousTreasury plugin) {
        this.plugin = plugin;
        itemFolder = new File(plugin.getDataFolder(), "items");
        if (!itemFolder.exists()) {
            itemFolder.mkdirs();
        }
        List<NuminousItemType> itemTypes = Arrays.stream(itemFolder.listFiles()).map(itemFile -> {
            YamlConfiguration itemConfiguration = YamlConfiguration.loadConfiguration(itemFile);
            NuminousItemType itemType =  itemConfiguration.getObject("item-type", NuminousItemType.class);
            if (itemType == null) {
                plugin.getLogger().warning("Failed to load item type from " + itemFile.getName());
            }
            return itemType;
        }).filter(Objects::nonNull).toList();
        itemTypesById.putAll(itemTypes.stream().collect(Collectors.toMap(NuminousItemType::getId, item -> item)));
        itemTypesByName.putAll(itemTypes.stream().collect(Collectors.toMap(item -> item.getName().toLowerCase(), item -> item)));
        plugin.getLogger().info("Loaded " + itemTypes.size() + " item types");
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public NuminousItemType getItemTypeById(String id) {
        return itemTypesById.get(id);
    }

    public NuminousItemType getItemTypeByName(String name) {
        return itemTypesByName.get(name.toLowerCase());
    }

    public List<NuminousItemType> getItemTypes() {
        return itemTypesById.values().stream().sorted().toList();
    }

    public void save(NuminousItemType itemType) throws IOException {
        YamlConfiguration itemConfiguration = new YamlConfiguration();
        itemConfiguration.set("item-type", itemType);
        File itemFile = new File(itemFolder, itemType.getId() + ".yml");
        itemConfiguration.save(itemFile);
    }

}
