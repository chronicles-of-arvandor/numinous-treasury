package net.arvandor.numinoustreasury.droptable

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.Random

@SerializableAs("NuminousDropTable")
class NuminousDropTable(val id: String, private val items: List<NuminousDropTableItem>) :
    ConfigurationSerializable, Comparable<NuminousDropTable> {
    private val random = Random()

    fun chooseItem(): NuminousDropTableItem? {
        val chanceSum =
            items.sumOf { obj: NuminousDropTableItem -> obj.chance }
        val choice = random.nextDouble(chanceSum)
        var sum = 0.0
        for (item in items) {
            sum += item.chance
            if (sum > choice) return item
        }
        return null
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "items" to items,
        )
    }

    override fun compareTo(other: NuminousDropTable): Int {
        return id.compareTo(other.id)
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): NuminousDropTable {
            return NuminousDropTable(
                serialized["id"] as String,
                serialized["items"] as List<NuminousDropTableItem>,
            )
        }
    }
}
