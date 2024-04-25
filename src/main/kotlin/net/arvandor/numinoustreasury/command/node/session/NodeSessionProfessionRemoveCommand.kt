package net.arvandor.numinoustreasury.command.node.session

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class NodeSessionProfessionRemoveCommand : CommandExecutor, TabCompleter {
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
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        if (args.size > 0) {
            val professionName = args.joinToString(" ")
            var profession = professionService.getProfessionById(professionName)
            if (profession == null) {
                profession = professionService.getProfessionByName(professionName)
            }
            if (profession == null) {
                sender.sendMessage(ChatColor.RED.toString() + "There is no profession by that name.")
                return true
            }
            session.removeRequiredProfessionLevel(profession)
            session.display(sender)
        } else {
            sender.sendMessage(ChatColor.WHITE.toString() + "Select a profession: ")
            professionService.professions.forEach { profession ->
                sender.spigot().sendMessage(
                    *ComponentBuilder(profession.name)
                        .color(ChatColor.GRAY)
                        .event(
                            ClickEvent(
                                RUN_COMMAND,
                                "/node session profession remove " + profession.id,
                            ),
                        )
                        .event(
                            HoverEvent(
                                SHOW_TEXT,
                                Text(
                                    ComponentBuilder("Click here to remove " + profession.name)
                                        .color(ChatColor.GRAY)
                                        .create(),
                                ),
                            ),
                        )
                        .create(),
                )
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        return if (args.isEmpty()) {
            professionService.professions
                .flatMap { profession ->
                    listOf(
                        profession.id,
                        profession.name,
                    )
                }
        } else {
            professionService.professions
                .flatMap { profession ->
                    listOf(
                        profession.id,
                        profession.name,
                    )
                }
                .filter { name: String ->
                    name.lowercase()
                        .startsWith(args.joinToString(" ").lowercase())
                }
        }
    }
}
