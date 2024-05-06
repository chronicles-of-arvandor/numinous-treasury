package net.arvandor.numinoustreasury.item.action

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.event.player.PlayerItemConsumeEvent

interface NuminousOnEat : ConfigurationSerializable {
    fun onEat(event: PlayerItemConsumeEvent)
}
