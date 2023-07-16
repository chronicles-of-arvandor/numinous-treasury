package net.arvandor.numinoustreasury.command.node.session;

import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeSessionCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    private final NodeSessionNameCommand nodeSessionNameCommand;
    private final NodeSessionProfessionCommand nodeSessionProfessionCommand;
    private final NodeSessionExperienceCommand nodeSessionExperienceCommand;
    private final NodeSessionStaminaCommand nodeSessionStaminaCommand;
    private final NodeSessionDropTableCommand nodeSessionDropTableCommand;
    private final NodeSessionEntranceAreaCommand nodeSessionEntranceAreaCommand;
    private final NodeSessionEntranceDestinationCommand nodeSessionEntranceDestinationCommand;
    private final NodeSessionExitAreaCommand nodeSessionExitAreaCommand;
    private final NodeSessionExitDestinationCommand nodeSessionExitDestinationCommand;
    private final NodeSessionAreaCommand nodeSessionAreaCommand;
    private final NodeSessionCreateCommand nodeSessionCreateCommand;

    private final List<String> nameAliases = List.of("name");
    private final List<String> professionAliases = List.of("profession", "prof");
    private final List<String> experienceAliases = List.of("experience", "exp");
    private final List<String> staminaAliases = List.of("stamina", "staminacost");
    private final List<String> dropTableAliases = List.of("droptable", "drops");
    private final List<String> entranceAreaAliases = List.of("entrancearea");
    private final List<String> entranceDestinationAliases = List.of("entrancedestination", "entrancedest");
    private final List<String> exitAreaAliases = List.of("exitarea");
    private final List<String> exitDestinationAliases = List.of("exitdestination", "exitdest");
    private final List<String> areaAliases = List.of("area");
    private final List<String> createAliases = List.of("create");
    private final List<String> subcommands = new ArrayList<>(){{
        addAll(nameAliases);
        addAll(professionAliases);
        addAll(experienceAliases);
        addAll(staminaAliases);
        addAll(dropTableAliases);
        addAll(entranceAreaAliases);
        addAll(entranceDestinationAliases);
        addAll(exitAreaAliases);
        addAll(exitDestinationAliases);
        addAll(areaAliases);
        addAll(createAliases);
    }};

    public NodeSessionCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
        nodeSessionNameCommand = new NodeSessionNameCommand(plugin);
        nodeSessionProfessionCommand = new NodeSessionProfessionCommand(plugin);
        nodeSessionExperienceCommand = new NodeSessionExperienceCommand(plugin);
        nodeSessionStaminaCommand = new NodeSessionStaminaCommand(plugin);
        nodeSessionDropTableCommand = new NodeSessionDropTableCommand();
        nodeSessionEntranceAreaCommand = new NodeSessionEntranceAreaCommand();
        nodeSessionEntranceDestinationCommand = new NodeSessionEntranceDestinationCommand();
        nodeSessionExitAreaCommand = new NodeSessionExitAreaCommand();
        nodeSessionExitDestinationCommand = new NodeSessionExitDestinationCommand();
        nodeSessionAreaCommand = new NodeSessionAreaCommand();
        nodeSessionCreateCommand = new NodeSessionCreateCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /node session [name|profession|experience|stamina|droptable|entrancearea|entrancedest|exitarea|exitdest|area|create]");
            return true;
        }
        if (nameAliases.contains(args[0].toLowerCase())) {
            return nodeSessionNameCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (professionAliases.contains(args[0].toLowerCase())) {
            return nodeSessionProfessionCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (experienceAliases.contains(args[0].toLowerCase())) {
            return nodeSessionExperienceCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (staminaAliases.contains(args[0].toLowerCase())) {
            return nodeSessionStaminaCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (dropTableAliases.contains(args[0].toLowerCase())) {
            return nodeSessionDropTableCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (entranceAreaAliases.contains(args[0].toLowerCase())) {
            return nodeSessionEntranceAreaCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (entranceDestinationAliases.contains(args[0].toLowerCase())) {
            return nodeSessionEntranceDestinationCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (exitAreaAliases.contains(args[0].toLowerCase())) {
            return nodeSessionExitAreaCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (exitDestinationAliases.contains(args[0].toLowerCase())) {
            return nodeSessionExitDestinationCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (areaAliases.contains(args[0].toLowerCase())) {
            return nodeSessionAreaCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else if (createAliases.contains(args[0].toLowerCase())) {
            return nodeSessionCreateCommand.onCommand(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
        } else {
            sender.sendMessage(RED + "Usage: /node session [name|profession|experience|stamina|droptable|entrancearea|entrancedest|exitarea|exitdest|area|create]");
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
            if (nameAliases.contains(args[0].toLowerCase())) {
                return nodeSessionNameCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (professionAliases.contains(args[0].toLowerCase())) {
                return nodeSessionProfessionCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (experienceAliases.contains(args[0].toLowerCase())) {
                return nodeSessionExperienceCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (staminaAliases.contains(args[0].toLowerCase())) {
                return nodeSessionStaminaCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (dropTableAliases.contains(args[0].toLowerCase())) {
                return nodeSessionDropTableCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (entranceAreaAliases.contains(args[0].toLowerCase())) {
                return nodeSessionEntranceAreaCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (entranceDestinationAliases.contains(args[0].toLowerCase())) {
                return nodeSessionEntranceDestinationCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (exitAreaAliases.contains(args[0].toLowerCase())) {
                return nodeSessionExitAreaCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (exitDestinationAliases.contains(args[0].toLowerCase())) {
                return nodeSessionExitDestinationCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (areaAliases.contains(args[0].toLowerCase())) {
                return nodeSessionAreaCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else if (createAliases.contains(args[0].toLowerCase())) {
                return nodeSessionCreateCommand.onTabComplete(sender, command, label, Arrays.stream(args).skip(1).toArray(String[]::new));
            } else {
                return List.of();
            }
        }
    }
}
