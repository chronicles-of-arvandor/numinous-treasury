package net.kingdommc.darkages.numinoustreasury.listener;

import net.kingdommc.darkages.numinoustreasury.item.NuminousItemStack;
import net.kingdommc.darkages.numinoustreasury.item.action.NuminousOnEat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.List;

public final class PlayerItemConsumeListener implements Listener {

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        NuminousItemStack numinousItemStack = NuminousItemStack.fromItemStack(event.getItem());
        if (numinousItemStack == null) return;
        List<NuminousOnEat> onEat = numinousItemStack.getItemType().getOnEat();
        for (NuminousOnEat action : onEat) {
            action.onEat(event);
        }
    }

}
