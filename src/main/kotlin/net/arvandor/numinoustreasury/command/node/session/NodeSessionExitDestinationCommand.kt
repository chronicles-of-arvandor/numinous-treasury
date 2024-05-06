package net.arvandor.numinoustreasury.command.node.session

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class NodeSessionExitDestinationCommand : CommandExecutor, TabCompleter {
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
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to create nodes.")
            return true
        }
        val nodeService =
            Services.INSTANCE.get(
                NuminousNodeService::class.java,
            )
        var session = nodeService.getNodeCreationSession(sender)
        if (session == null) {
            session = NuminousNodeCreationSession()
            nodeService.setNodeCreationSession(sender, session)
        }
        session.exitWarpDestination = sender.location
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
