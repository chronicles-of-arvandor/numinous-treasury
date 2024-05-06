package net.arvandor.numinoustreasury.droptable

import com.rpkit.core.service.Service
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Level

class NuminousDropTableService(private val plugin: NuminousTreasury) : Service {
    private val dropTablesById: MutableMap<String, NuminousDropTable> = HashMap()

    init {
        val dropTableFolder = File(plugin.dataFolder, "drop-tables")
        if (!dropTableFolder.exists()) {
            dropTableFolder.mkdirs()
        }
        val dropTables =
            dropTableFolder.listFiles().mapNotNull { dropTableFile ->
                val dropTableConfiguration =
                    YamlConfiguration.loadConfiguration(
                        dropTableFile,
                    )
                val dropTable = dropTableConfiguration.getObject("drop-table", NuminousDropTable::class.java)
                if (dropTable == null) {
                    plugin.logger.log(
                        Level.WARNING,
                        "Failed to load drop table from file " + dropTableFile.name,
                    )
                    return@mapNotNull null
                }
                return@mapNotNull dropTable
            }
        dropTablesById.putAll(
            dropTables.associateBy { it.id },
        )
        plugin.logger.info("Loaded " + dropTables.size + " drop tables")
    }

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    fun getDropTableById(id: String?): NuminousDropTable? {
        return dropTablesById[id]
    }

    val dropTables: List<NuminousDropTable>
        get() = dropTablesById.values.sortedWith { a, b -> a.compareTo(b) }
}
