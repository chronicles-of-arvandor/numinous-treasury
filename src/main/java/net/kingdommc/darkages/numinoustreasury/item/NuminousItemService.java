package net.kingdommc.darkages.numinoustreasury.item;

import com.rpkit.core.service.Service;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.item.action.RestoreHunger;
import net.kingdommc.darkages.numinoustreasury.measurement.Weight;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;
import static net.kingdommc.darkages.numinoustreasury.measurement.WeightUnit.G;

public final class NuminousItemService implements Service {

    private final NuminousTreasury plugin;
    private final Map<String, NuminousItemType> itemTypesById = new HashMap<>();
    private final Map<String, NuminousItemType> itemTypesByName = new HashMap<>();

    public NuminousItemService(NuminousTreasury plugin) {
        this.plugin = plugin;
        File itemFolder = new File(plugin.getDataFolder(), "items");
        if (!itemFolder.exists()) {
            itemFolder.mkdirs();
            saveExampleItems(itemFolder);
        }
        List<NuminousItemType> itemTypes = Arrays.stream(itemFolder.listFiles()).map(itemFile -> {
            YamlConfiguration itemConfiguration = YamlConfiguration.loadConfiguration(itemFile);
            return itemConfiguration.getObject("item-type", NuminousItemType.class);
        }).toList();
        itemTypesById.putAll(itemTypes.stream().collect(Collectors.toMap(NuminousItemType::getId, item -> item)));
        itemTypesByName.putAll(itemTypes.stream().collect(Collectors.toMap(NuminousItemType::getName, item -> item)));
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    private void saveExampleItems(File itemFolder) {
        File orangeFile = new File(itemFolder, "orange.yml");
        YamlConfiguration orangeConfig = new YamlConfiguration();
        ItemStack orangeMinecraftItem = new ItemStack(Material.APPLE);
        ItemMeta orangeMeta = orangeMinecraftItem.getItemMeta();
        if (orangeMeta != null) {
            orangeMeta.setDisplayName(ChatColor.of("#ff7f00") + "Orange");
            orangeMeta.setLore(List.of(
                    ChatColor.of("#fc9928") + "A juicy, orange fruit.",
                    ChatColor.of("#fc9928") + "What came first, the colour, or the fruit?"
            ));
            orangeMeta.setCustomModelData(1);
            orangeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        orangeMinecraftItem.setItemMeta(orangeMeta);
        orangeConfig.set("item-type", new NuminousItemType(
                plugin,
                "orange",
                "Orange",
                NuminousItemCategory.FLORA,
                NuminousRarity.COMMON,
                orangeMinecraftItem,
                List.of(new RestoreHunger(2, 3f)),
                List.of(),
                List.of(),
                new Weight(150, G)
        ));
        try {
            orangeConfig.save(orangeFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save example item config", exception);
        }
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

}
