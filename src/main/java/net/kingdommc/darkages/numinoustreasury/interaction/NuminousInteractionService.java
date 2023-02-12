package net.kingdommc.darkages.numinoustreasury.interaction;

import com.rpkit.core.service.Service;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NuminousInteractionService implements Service {

    private final NuminousTreasury plugin;
    private final Map<String, NuminousInteractionStatus> interactionStatus;

    public NuminousInteractionService(NuminousTreasury plugin) {
        this.plugin = plugin;
        interactionStatus = new ConcurrentHashMap<>();
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public NuminousInteractionStatus getInteractionStatus(Player player) {
        return interactionStatus.get(player.getUniqueId().toString());
    }

    public void setInteractionStatus(Player player, NuminousInteractionStatus status) {
        if (status == null) {
            interactionStatus.remove(player.getUniqueId().toString());
        } else {
            interactionStatus.put(player.getUniqueId().toString(), status);
        }
    }

}
