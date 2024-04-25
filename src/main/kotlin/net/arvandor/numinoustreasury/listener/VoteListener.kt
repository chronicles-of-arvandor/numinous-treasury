package net.arvandor.numinoustreasury.listener

import com.rpkit.core.service.Services
import com.vexsoftware.votifier.model.VotifierEvent
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import net.arvandor.numinoustreasury.stamina.StaminaTier
import net.md_5.bungee.api.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jooq.DSLContext
import kotlin.math.min

class VoteListener(private val plugin: NuminousTreasury) : Listener {
    private val voteStamina = plugin.config.getInt("stamina.vote-reward")

    @EventHandler
    fun onVote(event: VotifierEvent) {
        val username = event.vote.username
        val player = plugin.server.getOfflinePlayer(username)
        val staminaService =
            Services.INSTANCE.get(
                NuminousStaminaService::class.java,
            )
        staminaService.getAndUpdateStamina(
            player,
            { dsl: DSLContext, oldStamina: Int ->
                staminaService.setStamina(
                    dsl,
                    player,
                    min((oldStamina + voteStamina), staminaService.maxStamina),
                )
            },
            { oldStamina: Int, newStamina: Int ->
                val onlinePlayer = player.player
                if (onlinePlayer != null) {
                    val transitionMessage =
                        StaminaTier.messageForStaminaTransition(
                            oldStamina,
                            newStamina,
                            staminaService.maxStamina,
                        )
                    onlinePlayer.sendMessage(ChatColor.GREEN.toString() + "You feel some of your energy replenish.")
                    if (transitionMessage != null) {
                        onlinePlayer.sendMessage(transitionMessage)
                    }
                }
            },
        )
    }
}
