package net.arvandor.numinoustreasury.item.action;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.player.PlayerInteractEvent;

public interface NuminousOnInteractBlock extends ConfigurationSerializable {
    void onInteractBlock(PlayerInteractEvent event);
}
