package net.arvandor.numinoustreasury.command.stamina

import net.arvandor.numinoustreasury.NuminousTreasury
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class StaminaCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val staminaViewCommand = StaminaViewCommand(plugin)
    private val staminaSetCommand = StaminaSetCommand(plugin)

    private val viewAliases = listOf("view", "show")
    private val setAliases = listOf("set")
    private val subcommands = viewAliases + setAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.size < 1) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /stamina [set|view]")
            return true
        }
        when (args[0].lowercase()) {
            in viewAliases -> {
                return staminaViewCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in setAliases -> {
                return staminaSetCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            else -> {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /stamina [set|view]")
                return true
            }
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
                    in viewAliases -> {
                        staminaViewCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    }
                    in setAliases -> {
                        staminaSetCommand.onTabComplete(
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
