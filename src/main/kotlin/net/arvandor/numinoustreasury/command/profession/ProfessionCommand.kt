package net.arvandor.numinoustreasury.command.profession

import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.command.profession.experience.ProfessionExperienceCommand
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ProfessionCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val professionSetCommand = ProfessionSetCommand()
    private val professionViewCommand = ProfessionViewCommand()
    private val professionListCommand = ProfessionListCommand()
    private val professionExperienceCommand = ProfessionExperienceCommand(plugin)

    private val setAliases = listOf("set")
    private val viewAliases = listOf("view", "show", "info")
    private val listAliases = listOf("list")
    private val experienceAliases = listOf("experience", "exp", "xp")

    private val subcommands = setAliases + viewAliases + listAliases + experienceAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.size < 1) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /profession [set|view|list|experience]")
            return true
        }
        if (setAliases.contains(args[0].lowercase())) {
            return professionSetCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (viewAliases.contains(args[0].lowercase())) {
            return professionViewCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (listAliases.contains(args[0].lowercase())) {
            return professionListCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (experienceAliases.contains(args[0].lowercase())) {
            return professionExperienceCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /profession [set|view|list|experience]")
            return true
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        return when {
            args.size < 1 -> subcommands
            args.size == 1 ->
                subcommands
                    .filter { subcommand -> subcommand.startsWith(args[0].lowercase()) }
            else ->
                when (args[0].lowercase()) {
                    in setAliases ->
                        professionSetCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    in viewAliases ->
                        professionViewCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    in listAliases ->
                        professionListCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    in experienceAliases ->
                        professionExperienceCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    else -> listOf()
                }
        }
    }
}
