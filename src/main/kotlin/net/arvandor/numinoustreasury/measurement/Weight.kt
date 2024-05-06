package net.arvandor.numinoustreasury.measurement

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Weight")
class Weight(val value: Double, val unit: WeightUnit) : Comparable<Weight>, ConfigurationSerializable {
    fun to(unit: WeightUnit): Weight {
        return Weight((value / this.unit.scaleFactor) * unit.scaleFactor, unit)
    }

    fun multiply(amount: Int): Weight {
        return Weight(value * amount, unit)
    }

    override fun compareTo(other: Weight): Int {
        return Math.round((value / unit.scaleFactor) - (other.value / other.unit.scaleFactor)).toInt()
    }

    override fun toString(): String {
        return if (value == value.toLong().toDouble()) {
            String.format("%.0f%s", value, unit.name)
        } else {
            String.format("%s%s", value, unit.name)
        }
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "value" to value,
            "unit" to unit.name,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): Weight {
            return Weight(
                (serialized["value"] as Number).toDouble(),
                WeightUnit.getByName(serialized["unit"] as String)!!,
            )
        }
    }
}
