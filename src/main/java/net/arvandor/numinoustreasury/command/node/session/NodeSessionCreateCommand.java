package net.arvandor.numinoustreasury.command.node.session;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.area.Area;
import net.arvandor.numinoustreasury.node.NuminousNode;
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeSessionCreateCommand implements CommandExecutor, TabCompleter {
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
            sender.sendMessage(RED + "You do not have an active node creation session. Please use /node create first to start creating a node.");
            return true;
        }
        if (!session.isValid()) {
            sender.sendMessage(RED + "Your node is not correctly configured. Please ensure that all attributes are set, there is at least one required profession, and that the corners of the entrance, exit and area are in the same world as the other one in that area.");
            return true;
        }
        nodeService.save(new NuminousNode(
                UUID.randomUUID().toString(),
                session.getName(),
                session.getRequiredProfessionLevel(),
                session.getExperience(),
                session.getStaminaCost(),
                session.getDropTable(),
                new Area(
                        session.getEntranceAreaLocation1().getWorld(),
                        session.getEntranceAreaLocation1(),
                        session.getEntranceAreaLocation2()
                ),
                session.getEntranceWarpDestination(),
                new Area(
                        session.getExitAreaLocation1().getWorld(),
                        session.getExitAreaLocation1(),
                        session.getExitAreaLocation2()
                ),
                session.getExitWarpDestination(),
                new Area(
                        session.getAreaLocation1().getWorld(),
                        session.getAreaLocation1(),
                        session.getAreaLocation2()
                )
        ), () -> {
            nodeService.setNodeCreationSession(player, null);
            sender.sendMessage(GREEN + "Node created.");
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
