package net.arvandor.numinoustreasury.droptable

import net.arvandor.numinoustreasury.item.NuminousItemStack
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("NuminousDropTableItem")
class NuminousDropTableItem(val items: List<NuminousItemStack>, val chance: Double) :
    ConfigurationSerializable, Comparable<NuminousDropTableItem> {
    override fun serialize(): Map<String, Any?> {
        return mapOf(
            "items" to items,
            "chance" to chance,
        )
    }

    override fun compareTo(other: NuminousDropTableItem): Int {
        return other.chance.compareTo(chance)
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): NuminousDropTableItem {
            return NuminousDropTableItem(
                serialized["items"] as List<NuminousItemStack>,
                (serialized["chance"] as Number).toDouble(),
            )
        }
    }
}
