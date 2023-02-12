package net.kingdommc.darkages.numinoustreasury.command.node;

import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import net.kingdommc.darkages.numinoustreasury.command.node.session.NodeSessionCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeCommand implements CommandExecutor, TabCompleter {

    private final NodeCreateCommand nodeCreateCommand;
    private final NodeDeleteCommand nodeDeleteCommand;
    private final NodeSessionCommand nodeSessionCommand;

    private final List<String> createAliases = List.of("create", "new", "c", "n");
    private final List<String> deleteAliases = List.of("delete", "remove", "del", "rm", "d", "r");
    private final List<String> sessionAliases = List.of("session", "s");
    private final List<String> subcommands = new ArrayList<>(){{
        addAll(createAliases);
        addAll(deleteAliases);
        addAll(sessionAliases);
    }};

    public NodeCommand(NuminousTreasury plugin) {
        nodeCreateCommand = new NodeCreateCommand(plugin);
        nodeDeleteCommand = new NodeDeleteCommand(plugin);
        nodeSessionCommand = new NodeSessionCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /node [create|delete|session]");
            return true;
        }
        if (createAliases.contains(args[0].toLowerCase())) {
            return nodeCreateCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (deleteAliases.contains(args[0].toLowerCase())) {
            return nodeDeleteCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (sessionAliases.contains(args[0].toLowerCase())) {
            return nodeSessionCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /node [create|delete|session]");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return subcommands;
        } else if (args.length == 1) {
            return subcommands.stream().filter(subcommand -> subcommand.startsWith(args[0].toLowerCase())).toList();
        } else {
            if (createAliases.contains(args[0].toLowerCase())) {
                return nodeCreateCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (deleteAliases.contains(args[0].toLowerCase())) {
                return nodeDeleteCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (sessionAliases.contains(args[0].toLowerCase())) {
                return nodeSessionCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
