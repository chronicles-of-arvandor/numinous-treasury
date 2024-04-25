package net.arvandor.numinoustreasury.listener

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.node.NuminousNodeService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        val toLocation = event.to
        if (toLocation != null) {
            val nodeWithEntranceAtLocation = nodeService.getNodeWithEntranceAtLocation(toLocation)
            if (nodeWithEntranceAtLocation != null) {
                event.setTo(nodeWithEntranceAtLocation.entranceWarpDestination)
                return
            }
            val nodeWithExitAtLocation = nodeService.getNodeWithExitAtLocation(toLocation)
            if (nodeWithExitAtLocation != null) {
                event.setTo(nodeWithExitAtLocation.exitWarpDestination)
                return
            }
        }
    }
}
