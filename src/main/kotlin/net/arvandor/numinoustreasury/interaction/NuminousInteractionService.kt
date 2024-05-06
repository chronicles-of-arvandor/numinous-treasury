package net.arvandor.numinoustreasury.interaction

import com.rpkit.core.service.Service
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class NuminousInteractionService(private val plugin: NuminousTreasury) : Service {
    private val interactionStatus: MutableMap<String, NuminousInteractionStatus> =
        ConcurrentHashMap()

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    fun getInteractionStatus(player: Player): NuminousInteractionStatus? {
        return interactionStatus[player.uniqueId.toString()]
    }

    fun setInteractionStatus(
        player: Player,
        status: NuminousInteractionStatus?,
    ) {
        if (status == null) {
            interactionStatus.remove(player.uniqueId.toString())
        } else {
            interactionStatus[player.uniqueId.toString()] = status
        }
    }
}
