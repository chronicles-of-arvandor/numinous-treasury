package net.arvandor.numinoustreasury.command.node;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.node.NuminousNode;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Stream;

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeDeleteCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    public NodeDeleteCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.node.delete")) {
            sender.sendMessage(RED + "You do not have permission to delete nodes.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /node delete [node]");
            return true;
        }
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        String nodeName = String.join(" ", args);
        NuminousNode node = nodeService.getNodeById(nodeName);
        if (node == null) node = nodeService.getNodeByName(nodeName);
        if (node == null) {
            sender.sendMessage(RED + "There is no node by that name.");
            return true;
        }
        final NuminousNode finalNode = node;
        nodeService.delete(node, () -> sender.sendMessage(GREEN + "Node \"" + finalNode.getName() + "\" deleted."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        if (args.length == 0) {
            return nodeService.getNodes().stream().flatMap(node -> Stream.of(node.getId(), node.getName())).toList();
        } else {
            return nodeService.getNodes().stream().flatMap(node -> Stream.of(node.getId(), node.getName()))
                    .filter(name -> name.toLowerCase().startsWith(String.join(" ", args).toLowerCase()))
                    .toList();
        }
    }
}
