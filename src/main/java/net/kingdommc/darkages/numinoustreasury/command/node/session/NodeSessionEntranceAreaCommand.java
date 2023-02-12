package net.kingdommc.darkages.numinoustreasury.command.node.session;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.interaction.NuminousInteractionService;
import net.kingdommc.darkages.numinoustreasury.node.NuminousNodeCreationSession;
import net.kingdommc.darkages.numinoustreasury.node.NuminousNodeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

import static net.kingdommc.darkages.numinoustreasury.interaction.NuminousInteractionStatus.SELECTING_ENTRANCE_AREA_1;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeSessionEntranceAreaCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.node.create")) {
            sender.sendMessage(RED + "You do not have permission to create nodes.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to create nodes.");
            return true;
        }
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        NuminousNodeCreationSession session = nodeService.getNodeCreationSession(player);
        if (session == null) {
            session = new NuminousNodeCreationSession();
            nodeService.setNodeCreationSession(player, session);
        }
        NuminousInteractionService interactionService = Services.INSTANCE.get(NuminousInteractionService.class);
        interactionService.setInteractionStatus(player, SELECTING_ENTRANCE_AREA_1);
        sender.sendMessage(GREEN + "Please select location 1.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
