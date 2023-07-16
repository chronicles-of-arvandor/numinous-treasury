package net.arvandor.numinoustreasury.droptable;

import com.rpkit.core.service.Service;
import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.NuminousItemService;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;

public final class NuminousDropTableService implements Service {

    private final NuminousTreasury plugin;
    private final Map<String, NuminousDropTable> dropTablesById = new HashMap<>();

    public NuminousDropTableService(NuminousTreasury plugin) {
        this.plugin = plugin;
        File dropTableFolder = new File(plugin.getDataFolder(), "drop-tables");
        if (!dropTableFolder.exists()) {
            dropTableFolder.mkdirs();
            saveExampleDropTables(dropTableFolder);
        }
        List<NuminousDropTable> dropTables = Arrays.stream(dropTableFolder.listFiles()).map(dropTableFile -> {
            YamlConfiguration dropTableConfiguration = YamlConfiguration.loadConfiguration(dropTableFile);
            return dropTableConfiguration.getObject("drop-table", NuminousDropTable.class);
        }).toList();
        dropTablesById.putAll(dropTables.stream().collect(Collectors.toMap(NuminousDropTable::getId, dropTable -> dropTable)));
        plugin.getLogger().info("Loaded " + dropTables.size() + " drop tables");
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public NuminousDropTable getDropTableById(String id) {
        return dropTablesById.get(id);
    }

    public List<NuminousDropTable> getDropTables() {
        return dropTablesById.values().stream().sorted().toList();
    }

    private void saveExampleDropTables(File dropTableFolder) {
        saveMining(dropTableFolder);
        saveWoodcutting(dropTableFolder);
    }

    private void saveMining(File dropTableFolder) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        File miningFile = new File(dropTableFolder, "mining.yml");
        YamlConfiguration miningConfig = new YamlConfiguration();
        miningConfig.set("drop-table", new NuminousDropTable(
                "mining",
                List.of(
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("carbon"))),
                                30
                        ),
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("iron_ore"))),
                                55
                        ),
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("iron_ore"), 2)),
                                10
                        ),
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("iron_ore"), 5)),
                                5
                        )
                )
        ));
        try {
            miningConfig.save(miningFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save mining drop table", exception);
        }
    }

    private void saveWoodcutting(File dropTableFolder) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        File woodcuttingFile = new File(dropTableFolder, "woodcutting.yml");
        YamlConfiguration woodcuttingConfig = new YamlConfiguration();
        woodcuttingConfig.set("drop-table", new NuminousDropTable(
                "woodcutting",
                List.of(
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("orange"))),
                                30
                        ),
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("wood"))),
                                55
                        ),
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("wood"), 2)),
                                10
                        ),
                        new NuminousDropTableItem(
                                List.of(new NuminousItemStack(itemService.getItemTypeById("wood"), 5)),
                                5
                        )
                )
        ));
        try {
            woodcuttingConfig.save(woodcuttingFile);
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save woodcutting drop table", exception);
        }
    }

}
