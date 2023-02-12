package net.kingdommc.darkages.numinoustreasury.command.node.session;

import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeSessionProfessionCommand implements CommandExecutor, TabCompleter {

    private final NodeSessionProfessionAddCommand nodeSessionProfessionAddCommand;
    private final NodeSessionProfessionRemoveCommand nodeSessionProfessionRemoveCommand;

    private final List<String> addAliases = List.of("add", "a");
    private final List<String> removeAliases = List.of("remove", "rm", "r");
    private final List<String> subcommands = new ArrayList<>(){{
        addAll(addAliases);
        addAll(removeAliases);
    }};

    public NodeSessionProfessionCommand(NuminousTreasury plugin) {
        nodeSessionProfessionAddCommand = new NodeSessionProfessionAddCommand(plugin);
        nodeSessionProfessionRemoveCommand = new NodeSessionProfessionRemoveCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /node session profession [add|remove]");
            return true;
        }
        if (addAliases.contains(args[0].toLowerCase())) {
            return nodeSessionProfessionAddCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (removeAliases.contains(args[0].toLowerCase())) {
            return nodeSessionProfessionRemoveCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /node session profession [add|remove]");
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
            if (addAliases.contains(args[0].toLowerCase())) {
                return nodeSessionProfessionAddCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (removeAliases.contains(args[0].toLowerCase())) {
                return nodeSessionProfessionRemoveCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
