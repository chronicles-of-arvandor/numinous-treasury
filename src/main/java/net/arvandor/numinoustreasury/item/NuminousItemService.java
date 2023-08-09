package net.arvandor.numinoustreasury.item;

import static java.util.logging.Level.SEVERE;
import static org.bukkit.Material.APPLE;
import static org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES;

import com.rpkit.core.service.Service;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.action.Blocked;
import net.arvandor.numinoustreasury.item.action.RestoreHunger;
import net.arvandor.numinoustreasury.measurement.Weight;
import net.arvandor.numinoustreasury.measurement.WeightUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private void saveExampleItems(File itemFolder) {
        saveOrange(itemFolder);
        saveWood(itemFolder);
        saveHandle(itemFolder);
        saveIronOre(itemFolder);
        savePigIronIngot(itemFolder);
        saveCarbon(itemFolder);
        saveSteelIngot(itemFolder);
        saveBlade(itemFolder);
        saveSteelSword(itemFolder);
    }

    private void saveOrange(File itemFolder) {
        File orangeFile = new File(itemFolder, "orange.yml");
        YamlConfiguration orangeConfig = new YamlConfiguration();
        ItemStack orangeMinecraftItem = new ItemStack(APPLE);
        ItemMeta orangeMeta = orangeMinecraftItem.getItemMeta();
        if (orangeMeta != null) {
            orangeMeta.setDisplayName(ChatColor.of("#ff7f00") + "Orange");
            orangeMeta.setLore(List.of(
                    ChatColor.of("#fc9928") + "A juicy, orange fruit.",
                    ChatColor.of("#fc9928") + "What came first, the colour, or the fruit?"
            ));
            orangeMeta.setCustomModelData(1);
            orangeMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        orangeMinecraftItem.setItemMeta(orangeMeta);
        orangeConfig.set("item-type", new NuminousItemType(
                plugin,
                "orange",
                "Orange",
                List.of(NuminousItemCategory.FOOD_AND_DRINK),
                NuminousRarity.COMMON,
                orangeMinecraftItem,
                List.of(new RestoreHunger(2, 3f)),
                List.of(),
                List.of(),
                new Weight(150, WeightUnit.G)
        ));
        try {
            orangeConfig.save(orangeFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save orange item", exception);
        }
    }

    private void saveWood(File itemFolder) {
        File woodFile = new File(itemFolder, "wood.yml");
        YamlConfiguration woodConfig = new YamlConfiguration();
        ItemStack woodMinecraftItem = new ItemStack(Material.OAK_LOG);
        ItemMeta woodMeta = woodMinecraftItem.getItemMeta();
        if (woodMeta != null) {
            woodMeta.setDisplayName(ChatColor.of("#875838") + "Wood");
            woodMeta.setLore(List.of(
                    ChatColor.of("#b78463") + "A log of wood.",
                    ChatColor.of("#b78463") + "Where wood you get one of these?"
            ));
            woodMeta.setCustomModelData(1);
            woodMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        woodMinecraftItem.setItemMeta(woodMeta);
        woodConfig.set("item-type", new NuminousItemType(
                plugin,
                "wood",
                "Wood",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.COMMON,
                woodMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(650, WeightUnit.KG)
        ));
        try {
            woodConfig.save(woodFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save wood item", exception);
        }
    }

    private void saveHandle(File itemFolder) {
        File handleFile = new File(itemFolder, "handle.yml");
        YamlConfiguration handleConfig = new YamlConfiguration();
        ItemStack handleMinecraftItem = new ItemStack(Material.STICK);
        ItemMeta handleMeta = handleMinecraftItem.getItemMeta();
        if (handleMeta != null) {
            handleMeta.setDisplayName(ChatColor.of("#875838") + "Handle");
            handleMeta.setLore(List.of(
                    ChatColor.of("#b78463") + "A handle for a sword or tool of some description.",
                    ChatColor.of("#b78463") + "Can you even handle its potential?"
            ));
            handleMeta.setCustomModelData(1);
            handleMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        handleMinecraftItem.setItemMeta(handleMeta);
        handleConfig.set("item-type", new NuminousItemType(
                plugin,
                "handle",
                "Handle",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.COMMON,
                handleMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(50, WeightUnit.G)
        ));
        try {
            handleConfig.save(handleFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save handle item", exception);
        }
    }

    private void saveIronOre(File itemFolder) {
        File ironOreFile = new File(itemFolder, "iron_ore.yml");
        YamlConfiguration ironOreConfig = new YamlConfiguration();
        ItemStack ironOreMinecraftItem = new ItemStack(Material.IRON_ORE);
        ItemMeta ironOreMeta = ironOreMinecraftItem.getItemMeta();
        if (ironOreMeta != null) {
            ironOreMeta.setDisplayName(ChatColor.of("#594038") + "Iron ore");
            ironOreMeta.setLore(List.of(
                    ChatColor.of("#7c5a4e") + "Raw iron ore.",
                    ChatColor.of("#7c5a4e") + "Ore-striking."
            ));
            ironOreMeta.setCustomModelData(1);
            ironOreMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        ironOreMinecraftItem.setItemMeta(ironOreMeta);
        ironOreConfig.set("item-type", new NuminousItemType(
                plugin,
                "iron_ore",
                "Iron ore",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.COMMON,
                ironOreMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(5000, WeightUnit.KG)
        ));
        try {
            ironOreConfig.save(ironOreFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save example iron ore item", exception);
        }
    }

    private void savePigIronIngot(File itemFolder) {
        File pigIronIngotFile = new File(itemFolder, "pig_iron_ingot.yml");
        YamlConfiguration pigIronIngotConfig = new YamlConfiguration();
        ItemStack pigIronIngotMinecraftItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta pigIronIngotMeta = pigIronIngotMinecraftItem.getItemMeta();
        if (pigIronIngotMeta != null) {
            pigIronIngotMeta.setDisplayName(ChatColor.of("#998b86") + "Pig iron ingot");
            pigIronIngotMeta.setLore(List.of(
                    ChatColor.of("#f2e3de") + "Smelted iron ore with high carbon content.",
                    ChatColor.of("#f2e3de") + "Looks a bit like a piglet if you squint?"
            ));
            pigIronIngotMeta.setCustomModelData(1);
            pigIronIngotMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        pigIronIngotMinecraftItem.setItemMeta(pigIronIngotMeta);
        pigIronIngotConfig.set("item-type", new NuminousItemType(
                plugin,
                "pig_iron_ingot",
                "Pig iron ingot",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.COMMON,
                pigIronIngotMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(25, WeightUnit.KG)
        ));
        try {
            pigIronIngotConfig.save(pigIronIngotFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save pig iron ingot item", exception);
        }
    }

    private void saveCarbon(File itemFolder) {
        File carbonFile = new File(itemFolder, "carbon.yml");
        YamlConfiguration carbonConfig = new YamlConfiguration();
        ItemStack carbonMinecraftItem = new ItemStack(Material.COAL);
        ItemMeta carbonMeta = carbonMinecraftItem.getItemMeta();
        if (carbonMeta != null) {
            carbonMeta.setDisplayName(ChatColor.of("#181919") + "Carbon");
            carbonMeta.setLore(List.of(
                    ChatColor.of("#2b2b2b") + "A black solid chunk of carbon."
            ));
            carbonMeta.setCustomModelData(1);
            carbonMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        carbonMinecraftItem.setItemMeta(carbonMeta);
        carbonConfig.set("item-type", new NuminousItemType(
                plugin,
                "carbon",
                "Carbon",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.COMMON,
                carbonMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(50, WeightUnit.G)
        ));
        try {
            carbonConfig.save(carbonFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save carbon item", exception);
        }
    }

    private void saveSteelIngot(File itemFolder) {
        File steelIngotFile = new File(itemFolder, "steel_ingot.yml");
        YamlConfiguration steelIngotConfig = new YamlConfiguration();
        ItemStack steelIngotMinecraftItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta steelIngotMeta = steelIngotMinecraftItem.getItemMeta();
        if (steelIngotMeta != null) {
            steelIngotMeta.setDisplayName(ChatColor.of("#54585b") + "Steel ingot");
            steelIngotMeta.setLore(List.of(
                    ChatColor.of("#9aa9b7") + "An ingot of steel.",
                    ChatColor.of("#9aa9b7") + "> \"But I gotcha back when it comes ta weapons and armor! ",
                    ChatColor.of("#9aa9b7") + "> I gotta lady boner harder dan steel for dat stuff!\"",
                    ChatColor.of("#9aa9b7") + " - Wynne, EOIV"
            ));
            steelIngotMeta.setCustomModelData(1);
            steelIngotMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        steelIngotMinecraftItem.setItemMeta(steelIngotMeta);
        steelIngotConfig.set("item-type", new NuminousItemType(
                plugin,
                "steel_ingot",
                "Steel ingot",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.COMMON,
                steelIngotMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(25, WeightUnit.KG)
        ));
        try {
            steelIngotConfig.save(steelIngotFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save steel ingot config", exception);
        }
    }

    private void saveBlade(File itemFolder) {
        File bladeFile = new File(itemFolder, "blade.yml");
        YamlConfiguration bladeConfig = new YamlConfiguration();
        ItemStack bladeMinecraftItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta bladeMeta = bladeMinecraftItem.getItemMeta();
        if (bladeMeta != null) {
            bladeMeta.setDisplayName(ChatColor.of("#54585b") + "Blade");
            bladeMeta.setLore(List.of(
                    ChatColor.of("#9aa9b7") + "The blade of a sword.",
                    ChatColor.of("#9aa9b7") + "Don't run anywhere with it!"
            ));
            bladeMeta.setCustomModelData(1);
            bladeMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        bladeMinecraftItem.setItemMeta(bladeMeta);
        bladeConfig.set("item-type", new NuminousItemType(
                plugin,
                "blade",
                "Blade",
                List.of(NuminousItemCategory.CRAFTING_MATERIAL),
                NuminousRarity.UNCOMMON,
                bladeMinecraftItem,
                List.of(new Blocked()),
                List.of(new Blocked()),
                List.of(new Blocked()),
                new Weight(1950, WeightUnit.G)
        ));
        try {
            bladeConfig.save(bladeFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save blade item", exception);
        }
    }

    private void saveSteelSword(File itemFolder) {
        File steelSwordFile = new File(itemFolder, "steel_sword.yml");
        YamlConfiguration steelSwordConfig = new YamlConfiguration();
        ItemStack steelSwordMinecraftItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta steelSwordMeta = steelSwordMinecraftItem.getItemMeta();
        if (steelSwordMeta != null) {
            steelSwordMeta.setDisplayName(ChatColor.of("#54585b") + "Steel sword");
            steelSwordMeta.setLore(List.of(
                    ChatColor.of("#9aa9b7") + "A well-crafted steel sword.",
                    ChatColor.of("#9aa9b7") + "Your reflection glimmers across its blade."
            ));
            steelSwordMeta.setCustomModelData(1);
            steelSwordMeta.addItemFlags(HIDE_ATTRIBUTES);
        }
        steelSwordMinecraftItem.setItemMeta(steelSwordMeta);
        steelSwordConfig.set("item-type", new NuminousItemType(
                plugin,
                "steel_sword",
                "Steel sword",
                List.of(NuminousItemCategory.SIMPLE_WEAPON, NuminousItemCategory.MELEE_WEAPON),
                NuminousRarity.RARE,
                steelSwordMinecraftItem,
                List.of(new Blocked()),
                List.of(),
                List.of(),
                new Weight(2, WeightUnit.KG)
        ));
        try {
            steelSwordConfig.save(steelSwordFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save example steel sword item", exception);
        }
    }

}
