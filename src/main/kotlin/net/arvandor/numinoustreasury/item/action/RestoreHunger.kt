package net.arvandor.numinoustreasury.item.action

import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot

@SerializableAs("RestoreHunger")
class RestoreHunger(val hunger: Int, val saturation: Float) :
    NuminousOnEat,
    NuminousOnInteractAir,
    NuminousOnInteractBlock {
    override fun onEat(event: PlayerItemConsumeEvent) {
        consumeItem(event, event.hand)
    }

    override fun onInteractAir(event: PlayerInteractEvent) {
        consumeItem(event, event.hand)
    }

    override fun onInteractBlock(event: PlayerInteractEvent) {
        consumeItem(event, event.hand)
    }

    private fun consumeItem(
        event: PlayerEvent,
        hand: EquipmentSlot?,
    ) {
        if (event is Cancellable) {
            event.isCancelled = true
        }
        if (hand == null) return
        val player = event.player
        val itemInUse = player.itemInUse ?: return
        val inventory = player.inventory
        if (itemInUse.amount == 1) {
            inventory.setItem(hand, null)
        } else {
            itemInUse.amount -= 1
        }
        player.foodLevel += hunger
        player.saturation += saturation
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "hunger" to hunger,
            "saturation" to saturation,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): RestoreHunger {
            return RestoreHunger(
                serialized["hunger"] as Int,
                (serialized["saturation"] as Double).toFloat(),
            )
        }
    }
}
