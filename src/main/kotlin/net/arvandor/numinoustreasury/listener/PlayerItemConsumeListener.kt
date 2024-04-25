package net.arvandor.numinoustreasury.listener

import net.arvandor.numinoustreasury.item.NuminousItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class PlayerItemConsumeListener : Listener {
    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val numinousItemStack: NuminousItemStack = NuminousItemStack.Companion.fromItemStack(event.item) ?: return
        val onEat = numinousItemStack.itemType.onEat
        for (action in onEat) {
            action.onEat(event)
        }
    }
}
