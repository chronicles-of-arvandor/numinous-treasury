package net.kingdommc.darkages.numinoustreasury.item.action;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public interface NuminousOnEat extends ConfigurationSerializable {
    void onEat(PlayerItemConsumeEvent event);
}
