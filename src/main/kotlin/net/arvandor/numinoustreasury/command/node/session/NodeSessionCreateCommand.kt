package net.arvandor.numinoustreasury.command.node.session

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.area.Area
import net.arvandor.numinoustreasury.node.NuminousNode
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.UUID

class NodeSessionCreateCommand : CommandExecutor, TabCompleter {
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
        val session = nodeService.getNodeCreationSession(sender)
        if (session == null) {
            sender.sendMessage(
                ChatColor.RED.toString() +
                    "You do not have an active node creation session. " +
                    "Please use /node create first to start creating a node.",
            )
            return true
        }
        if (!session.isValid) {
            sender.sendMessage(
                ChatColor.RED.toString() + "Your node is not correctly configured. Please ensure that all attributes " +
                    "are set, there is at least one required profession, and that the corners of the entrance, " +
                    "exit and area are in the same world as the other one in that area.",
            )
            return true
        }
        nodeService.save(
            NuminousNode(
                UUID.randomUUID().toString(),
                session.name,
                session.requiredProfessionLevel,
                session.experience,
                session.staminaCost,
                session.dropTable!!,
                Area(
                    session.entranceAreaLocation1!!.world!!,
                    session.entranceAreaLocation1!!,
                    session.entranceAreaLocation2!!,
                ),
                session.entranceWarpDestination!!,
                Area(
                    session.exitAreaLocation1!!.world!!,
                    session.exitAreaLocation1!!,
                    session.exitAreaLocation2!!,
                ),
                session.exitWarpDestination!!,
                Area(
                    session.areaLocation1!!.world!!,
                    session.areaLocation1!!,
                    session.areaLocation2!!,
                ),
            ),
        ) {
            nodeService.setNodeCreationSession(sender, null)
            sender.sendMessage(ChatColor.GREEN.toString() + "Node created.")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        return listOf()
    }
}
