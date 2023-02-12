package net.kingdommc.darkages.numinoustreasury.command.node;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.node.NuminousNodeCreationSession;
import net.kingdommc.darkages.numinoustreasury.node.NuminousNodeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeCreateCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    public NodeCreateCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.node.create")) {
            sender.sendMessage(RED + "You do not have permission to create nodes.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "This command may not be used from console.");
            return true;
        }
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        NuminousNodeCreationSession session = new NuminousNodeCreationSession();
        nodeService.setNodeCreationSession(player, session);
        session.display(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
