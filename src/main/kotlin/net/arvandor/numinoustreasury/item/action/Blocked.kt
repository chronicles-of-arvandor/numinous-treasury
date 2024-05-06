package net.arvandor.numinoustreasury.item.action

import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

@SerializableAs("Blocked")
class Blocked : NuminousOnEat, NuminousOnInteractAir, NuminousOnInteractBlock {
    override fun onEat(event: PlayerItemConsumeEvent) {
        event.isCancelled = true
    }

    override fun onInteractAir(event: PlayerInteractEvent) {
        event.isCancelled = true
    }

    override fun onInteractBlock(event: PlayerInteractEvent) {
        event.isCancelled = true
    }

    override fun serialize(): Map<String, Any> {
        return mapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>?): Blocked {
            return Blocked()
        }
    }
}
