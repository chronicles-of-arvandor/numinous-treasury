package net.kingdommc.darkages.numinoustreasury.command.stamina;

import net.kingdommc.darkages.numinoustreasury.NuminousTreasury;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.RED;

public final class StaminaCommand implements CommandExecutor, TabCompleter {

    private final StaminaViewCommand staminaViewCommand;
    private final StaminaSetCommand staminaSetCommand;

    private final List<String> viewAliases = List.of("view", "show");
    private final List<String> setAliases = List.of("set");
    private final List<String> subcommands = new ArrayList<>(){{
        addAll(viewAliases);
        addAll(setAliases);
    }};

    public StaminaCommand(NuminousTreasury plugin) {
        staminaViewCommand = new StaminaViewCommand(plugin);
        staminaSetCommand = new StaminaSetCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /stamina [set|view]");
            return true;
        }
        if (viewAliases.contains(args[0].toLowerCase())) {
            return staminaViewCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (setAliases.contains(args[0].toLowerCase())) {
            return staminaSetCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /stamina [set|view]");
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
                return staminaViewCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (setAliases.contains(args[0].toLowerCase())) {
                return staminaSetCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
