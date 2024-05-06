package net.arvandor.numinoustreasury.command.node

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class NodeCreateCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.node.create")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to create nodes.")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "This command may not be used from console.")
            return true
        }
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        val session = NuminousNodeCreationSession()
        nodeService.setNodeCreationSession(sender, session)
        session.display(sender)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        return listOf()
    }
}
