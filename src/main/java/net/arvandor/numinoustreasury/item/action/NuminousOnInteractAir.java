package net.arvandor.numinoustreasury.item.action;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.player.PlayerInteractEvent;

public interface NuminousOnInteractAir extends ConfigurationSerializable {
    void onInteractAir(PlayerInteractEvent event);
}
