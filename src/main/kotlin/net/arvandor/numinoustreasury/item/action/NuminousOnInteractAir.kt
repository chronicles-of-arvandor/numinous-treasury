package net.arvandor.numinoustreasury.item.action

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.event.player.PlayerInteractEvent

interface NuminousOnInteractAir : ConfigurationSerializable {
    fun onInteractAir(event: PlayerInteractEvent)
}
