package net.arvandor.numinoustreasury.command.profession.experience;

import static org.bukkit.ChatColor.RED;

import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ProfessionExperienceCommand implements CommandExecutor, TabCompleter {

    private final ProfessionExperienceSetCommand setCommand;
    private final ProfessionExperienceAddCommand addCommand;

    private final List<String> setAliases = List.of("set");
    private final List<String> addAliases = List.of("add");

    private final List<String> subcommands = new ArrayList<>(){{
        addAll(setAliases);
        addAll(addAliases);
    }};

    public ProfessionExperienceCommand(NuminousTreasury plugin) {
        setCommand = new ProfessionExperienceSetCommand(plugin);
        addCommand = new ProfessionExperienceAddCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /profession experience [set|add]");
            return true;
        }
        if (setAliases.contains(args[0].toLowerCase())) {
            return setCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (addAliases.contains(args[0].toLowerCase())) {
            return addCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /profession experience [set|add]");
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
            if (setAliases.contains(args[0].toLowerCase())) {
                return setCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (addAliases.contains(args[0].toLowerCase())) {
                return addCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
