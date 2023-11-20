package net.arvandor.numinoustreasury.command.numinouslog;

import static net.md_5.bungee.api.ChatColor.RED;

import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NuminousLogCommand implements CommandExecutor, TabCompleter {

    private final NuminousLogViewCommand numinousLogViewCommand;
    private final NuminousLogAddCommand numinousLogAddCommand;

    private final List<String> viewAliases = List.of("view", "show");
    private final List<String> addAliases = List.of("add", "append");

    private final List<String> subcommands = new ArrayList<>() {{
        addAll(viewAliases);
        addAll(addAliases);
    }};

    public NuminousLogCommand(NuminousTreasury plugin) {
        numinousLogViewCommand = new NuminousLogViewCommand(plugin);
        numinousLogAddCommand = new NuminousLogAddCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /numinouslog [view|add]");
            return true;
        }
        if (viewAliases.contains(args[0].toLowerCase())) {
            return numinousLogViewCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (addAliases.contains(args[0].toLowerCase())) {
            return numinousLogAddCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /numinouslog [view|add]");
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
            if (viewAliases.contains(args[0].toLowerCase())) {
                return numinousLogViewCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (addAliases.contains(args[0].toLowerCase())) {
                return numinousLogAddCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
