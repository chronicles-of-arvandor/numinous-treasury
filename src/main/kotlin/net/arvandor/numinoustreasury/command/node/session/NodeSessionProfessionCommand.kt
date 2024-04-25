package net.arvandor.numinoustreasury.command.node.session

import net.arvandor.numinoustreasury.NuminousTreasury
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NodeSessionProfessionCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val nodeSessionProfessionAddCommand = NodeSessionProfessionAddCommand(plugin)
    private val nodeSessionProfessionRemoveCommand = NodeSessionProfessionRemoveCommand()

    private val addAliases = listOf("add", "a")
    private val removeAliases = listOf("remove", "rm", "r")
    private val subcommands = addAliases + removeAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /node session profession [add|remove]")
            return true
        }
        when (args[0].lowercase()) {
            in addAliases -> return nodeSessionProfessionAddCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
            in removeAliases -> return nodeSessionProfessionRemoveCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
            else -> {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /node session profession [add|remove]")
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
            args.size == 1 -> subcommands.filter { subcommand: String -> subcommand.startsWith(args[0].lowercase()) }
            else ->
                when (args[0].lowercase()) {
                    in addAliases ->
                        nodeSessionProfessionAddCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    in removeAliases ->
                        nodeSessionProfessionRemoveCommand.onTabComplete(
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
