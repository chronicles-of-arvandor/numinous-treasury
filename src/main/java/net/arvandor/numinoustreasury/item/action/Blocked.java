package net.arvandor.numinoustreasury.item.action;

import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.Map;

@SerializableAs("Blocked")
public final class Blocked implements NuminousOnEat, NuminousOnInteractAir, NuminousOnInteractBlock {

    @Override
    public void onEat(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onInteractAir(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of();
    }

    public static Blocked deserialize(Map<String, Object> serialized) {
        return new Blocked();
    }

}
