package net.arvandor.numinoustreasury.listener;

import static net.md_5.bungee.api.ChatColor.GREEN;

import com.rpkit.core.service.Services;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService;
import net.arvandor.numinoustreasury.stamina.StaminaTier;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class VoteListener implements Listener {

    private final NuminousTreasury plugin;
    private final int voteStamina;

    public VoteListener(NuminousTreasury plugin) {
        this.plugin = plugin;
        this.voteStamina = plugin.getConfig().getInt("stamina.vote-reward");
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        String username = event.getVote().getUsername();
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(username);
        NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
        staminaService.getAndUpdateStamina(
                player,
                (dsl, oldStamina) -> {
                    staminaService.setStamina(dsl, player, Math.min(oldStamina + voteStamina, staminaService.getMaxStamina()));
                },
                (oldStamina, newStamina) -> {
                    Player onlinePlayer = player.getPlayer();
                    if (onlinePlayer != null) {
                        String transitionMessage = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, staminaService.getMaxStamina());
                        onlinePlayer.sendMessage(GREEN + "You feel some of your energy replenish.");
                        if (transitionMessage != null) {
                            onlinePlayer.sendMessage(transitionMessage);
                        }
                    }
                }
        );
    }

}
