package net.arvandor.numinoustreasury.profession

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("NuminousProfession")
class NuminousProfession(val id: String, val name: String) : ConfigurationSerializable {
    override fun serialize(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): NuminousProfession {
            return NuminousProfession(
                serialized["id"] as String,
                serialized["name"] as String,
            )
        }
    }
}
