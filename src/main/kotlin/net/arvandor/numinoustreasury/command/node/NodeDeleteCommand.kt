package net.arvandor.numinoustreasury.command.node

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NodeDeleteCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.node.delete")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to delete nodes.")
            return true
        }
        if (args.size < 1) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /node delete [node]")
            return true
        }
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        val nodeName = args.joinToString(" ")
        var node = nodeService.getNodeById(nodeName)
        if (node == null) node = nodeService.getNodeByName(nodeName)
        if (node == null) {
            sender.sendMessage(ChatColor.RED.toString() + "There is no node by that name.")
            return true
        }
        val finalNode = node
        nodeService.delete(node) { sender.sendMessage(ChatColor.GREEN.toString() + "Node \"" + finalNode.name + "\" deleted.") }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        return if (args.size == 0) {
            nodeService.nodes.flatMap { node -> listOf(node.id, node.name) }
        } else {
            nodeService.nodes.flatMap { node -> listOf(node.id, node.name) }
                .filter { name: String ->
                    name.lowercase()
                        .startsWith(args.joinToString(" ").lowercase())
                }
        }
    }
}
