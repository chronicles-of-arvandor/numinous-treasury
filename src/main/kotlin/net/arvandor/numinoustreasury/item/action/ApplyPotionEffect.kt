package net.arvandor.numinoustreasury.item.action

import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect

@SerializableAs("ApplyPotionEffect")
class ApplyPotionEffect(val potionEffects: List<PotionEffect>) :
    NuminousOnEat,
    NuminousOnInteractAir,
    NuminousOnInteractBlock {
    override fun onEat(event: PlayerItemConsumeEvent) {
        consumeItem(event)
    }

    override fun onInteractAir(event: PlayerInteractEvent) {
        consumeItem(event)
    }

    override fun onInteractBlock(event: PlayerInteractEvent) {
        consumeItem(event)
    }

    private fun consumeItem(event: PlayerEvent) {
        if (event is Cancellable) {
            event.isCancelled = true
        }
        val player = event.player
        val itemInUse = player.itemInUse ?: return
        val inventory = player.inventory
        if (itemInUse.amount == 1) {
            if (itemInUse === inventory.itemInMainHand) {
                inventory.setItemInMainHand(null)
            } else if (itemInUse === inventory.itemInOffHand) {
                inventory.setItemInOffHand(null)
            }
        } else {
            itemInUse.amount -= 1
        }
        player.addPotionEffects(potionEffects)
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "potion-effects" to potionEffects,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String?, Any?>): ApplyPotionEffect {
            return ApplyPotionEffect(
                serialized["potion-effects"] as List<PotionEffect>,
            )
        }
    }
}
