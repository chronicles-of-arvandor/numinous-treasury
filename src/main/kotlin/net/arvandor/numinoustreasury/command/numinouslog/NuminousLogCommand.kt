package net.arvandor.numinoustreasury.command.numinouslog

import net.arvandor.numinoustreasury.NuminousTreasury
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NuminousLogCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val numinousLogViewCommand = NuminousLogViewCommand(plugin)
    private val numinousLogAddCommand = NuminousLogAddCommand(plugin)

    private val viewAliases = listOf("view", "show")
    private val addAliases = listOf("add", "append")

    private val subcommands = viewAliases + addAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /numinouslog [view|add]")
            return true
        }
        if (viewAliases.contains(args[0].lowercase())) {
            return numinousLogViewCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (addAliases.contains(args[0].lowercase())) {
            return numinousLogAddCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /numinouslog [view|add]")
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
            args.size == 1 -> subcommands.filter { subcommand: String -> subcommand.startsWith(args[0].lowercase()) }
            else ->
                when (args[0].lowercase()) {
                    in viewAliases ->
                        numinousLogViewCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    in addAliases ->
                        numinousLogAddCommand.onTabComplete(
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
