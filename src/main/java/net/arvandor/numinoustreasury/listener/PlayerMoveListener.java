package net.arvandor.numinoustreasury.listener;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.node.NuminousNode;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        NuminousNode nodeWithEntranceAtLocation = nodeService.getNodeWithEntranceAtLocation(event.getTo());
        if (nodeWithEntranceAtLocation != null) {
            event.setTo(nodeWithEntranceAtLocation.getEntranceWarpDestination());
            return;
        }
        NuminousNode nodeWithExitAtLocation = nodeService.getNodeWithExitAtLocation(event.getTo());
        if (nodeWithExitAtLocation != null) {
            event.setTo(nodeWithExitAtLocation.getExitWarpDestination());
            return;
        }
    }

}
