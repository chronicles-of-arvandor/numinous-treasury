package net.arvandor.numinoustreasury.command.profession.experience

import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ProfessionExperienceCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val setCommand = ProfessionExperienceSetCommand(plugin)
    private val addCommand = ProfessionExperienceAddCommand(plugin)

    private val setAliases = listOf("set")
    private val addAliases = listOf("add")

    private val subcommands = setAliases + addAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(RED.toString() + "Usage: /profession experience [set|add]")
            return true
        }
        if (setAliases.contains(args[0].lowercase())) {
            return setCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (addAliases.contains(args[0].lowercase())) {
            return addCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else {
            sender.sendMessage(RED.toString() + "Usage: /profession experience [set|add]")
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
            args.isEmpty() -> subcommands
            args.size == 1 ->
                subcommands
                    .filter { subcommand -> subcommand.startsWith(args[0].lowercase()) }
            else ->
                when (args[0].lowercase()) {
                    in setAliases -> {
                        setCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    }
                    in addAliases -> {
                        addCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    }
                    else -> {
                        listOf()
                    }
                }
        }
    }
}
