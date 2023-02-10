package net.kingdommc.darkages.numinoustreasury.command.profession;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.RED;

public final class ProfessionCommand implements CommandExecutor, TabCompleter {

    private final ProfessionSetCommand professionSetCommand;
    private final ProfessionViewCommand professionViewCommand;
    private final ProfessionListCommand professionListCommand;

    private final List<String> setAliases = List.of("set");
    private final List<String> viewAliases = List.of("view", "show", "info");
    private final List<String> listAliases = List.of("list");
    private final List<String> subcommands = new ArrayList<>(){{
        addAll(setAliases);
        addAll(viewAliases);
        addAll(listAliases);
    }};

    public ProfessionCommand() {
        professionSetCommand = new ProfessionSetCommand();
        professionViewCommand = new ProfessionViewCommand();
        professionListCommand = new ProfessionListCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /profession [set|view|list]");
            return true;
        }
        if (setAliases.contains(args[0].toLowerCase())) {
            return professionSetCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (viewAliases.contains(args[0].toLowerCase())) {
            return professionViewCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (listAliases.contains(args[0].toLowerCase())) {
            return professionListCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /profession [set|view|list]");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return subcommands;
        } else if (args.length == 1) {
            return subcommands.stream().filter(subcommand -> subcommand.startsWith(args[0])).toList();
        } else {
            if (setAliases.contains(args[0].toLowerCase())) {
                return professionSetCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (viewAliases.contains(args[0].toLowerCase())) {
                return professionViewCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (listAliases.contains(args[0].toLowerCase())) {
                return professionListCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
