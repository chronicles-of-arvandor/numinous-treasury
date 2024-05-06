package net.arvandor.numinoustreasury.command.node

import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.command.node.session.NodeSessionCommand
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NodeCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val nodeCreateCommand = NodeCreateCommand(plugin)
    private val nodeDeleteCommand = NodeDeleteCommand(plugin)
    private val nodeSessionCommand = NodeSessionCommand(plugin)

    private val createAliases = listOf("create", "new", "c", "n")
    private val deleteAliases = listOf("delete", "remove", "del", "rm", "d", "r")
    private val sessionAliases = listOf("session", "s")
    private val subcommands = createAliases + deleteAliases + sessionAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.size < 1) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /node [create|delete|session]")
            return true
        }
        if (createAliases.contains(args[0].lowercase())) {
            return nodeCreateCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (deleteAliases.contains(args[0].lowercase())) {
            return nodeDeleteCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else if (sessionAliases.contains(args[0].lowercase())) {
            return nodeSessionCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /node [create|delete|session]")
            return true
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        return if (args.size < 1) {
            subcommands
        } else if (args.size == 1) {
            subcommands
                .filter { subcommand: String -> subcommand.startsWith(args[0].lowercase()) }
        } else {
            if (createAliases.contains(args[0].lowercase())) {
                nodeCreateCommand.onTabComplete(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            } else if (deleteAliases.contains(args[0].lowercase())) {
                nodeDeleteCommand.onTabComplete(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            } else if (sessionAliases.contains(args[0].lowercase())) {
                nodeSessionCommand.onTabComplete(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            } else {
                listOf<String>()
            }
        }
    }
}
