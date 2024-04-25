package net.arvandor.numinoustreasury.item

import com.rpkit.core.service.Service
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class NuminousItemService(private val plugin: NuminousTreasury) : Service {
    private val itemFolder = File(plugin.dataFolder, "items")
    private val itemTypesById: MutableMap<String, NuminousItemType> = mutableMapOf()
    private val itemTypesByName: MutableMap<String, NuminousItemType> = mutableMapOf()

    init {
        if (!itemFolder.exists()) {
            itemFolder.mkdirs()
        }
        val itemTypes =
            itemFolder.listFiles().mapNotNull { itemFile: File ->
                val itemConfiguration = YamlConfiguration.loadConfiguration(itemFile)
                val itemType = itemConfiguration.getObject("item-type", NuminousItemType::class.java)
                if (itemType == null) {
                    plugin.logger.warning("Failed to load item type from " + itemFile.name)
                }
                itemType
            }
        itemTypesById.putAll(itemTypes.associateBy { it.id })
        itemTypesByName.putAll(itemTypes.associateBy { it.name.lowercase() })
        plugin.logger.info("Loaded " + itemTypes.size + " item types")
    }

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    fun getItemTypeById(id: String?): NuminousItemType? {
        return itemTypesById[id]
    }

    fun getItemTypeByName(name: String): NuminousItemType? {
        return itemTypesByName[name.lowercase()]
    }

    val itemTypes: List<NuminousItemType>
        get() = itemTypesById.values.sortedWith { a, b -> a.compareTo(b) }

    @Throws(IOException::class)
    fun save(itemType: NuminousItemType) {
        val itemConfiguration = YamlConfiguration()
        itemConfiguration["item-type"] = itemType
        val itemFile = File(itemFolder, itemType.id + ".yml")
        itemConfiguration.save(itemFile)
    }
}
