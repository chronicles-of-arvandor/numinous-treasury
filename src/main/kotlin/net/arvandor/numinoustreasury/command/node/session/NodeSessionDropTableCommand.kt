package net.arvandor.numinoustreasury.command.node.session

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class NodeSessionDropTableCommand : CommandExecutor, TabCompleter {
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
        val dropTableService =
            Services.INSTANCE.get(
                NuminousDropTableService::class.java,
            )
        val dropTableSpecified = args.size > 1 && args[0].equals("set", ignoreCase = true)
        var page = 1
        if (args.size > 1 && args[0].equals("page", ignoreCase = true)) {
            try {
                page = args[1].toInt()
            } catch (exception: NumberFormatException) {
                sender.sendMessage(ChatColor.RED.toString() + "Page number must be an integer.")
                return true
            }
        }
        if (dropTableSpecified) {
            val dropTableId = args.drop(1).joinToString(" ")
            val dropTable = dropTableService.getDropTableById(dropTableId)
            if (dropTable == null) {
                sender.sendMessage(ChatColor.RED.toString() + "There is no drop table with that ID.")
                return true
            }
            session.dropTable = dropTable
            session.display(sender)
        } else {
            val dropTables = dropTableService.dropTables
            if (dropTables.isEmpty()) {
                sender.sendMessage(ChatColor.RED.toString() + "There are no drop tables configured.")
                return true
            }
            val title = TextComponent("Select a drop table: ")
            title.color = ChatColor.WHITE
            val view =
                PaginatedView.fromChatComponents(
                    arrayOf<BaseComponent>(
                        title,
                    ),
                    dropTables.map { dropTable ->
                        ComponentBuilder(
                            dropTable.id,
                        )
                            .color(ChatColor.GREEN)
                            .event(
                                ClickEvent(
                                    RUN_COMMAND,
                                    "/node session droptable set " + dropTable.id,
                                ),
                            )
                            .event(
                                HoverEvent(
                                    SHOW_TEXT,
                                    Text(
                                        ComponentBuilder("Click here to set the drop table to " + dropTable.id)
                                            .color(ChatColor.GRAY)
                                            .create(),
                                    ),
                                ),
                            )
                            .create()
                    },
                    ChatColor.GREEN.toString() + "< Previous",
                    "Click here to view the previous page",
                    ChatColor.GREEN.toString() + "Next >",
                    "Click here to view the next page",
                    { pageNumber: Int -> "Page $pageNumber" },
                    10,
                    { pageNumber: Int -> "/node session droptable page $pageNumber" },
                )
            if (view.isPageValid(page)) {
                view.sendPage(sender, page)
            } else {
                sender.sendMessage(ChatColor.RED.toString() + "Invalid page number.")
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        val dropTableService =
            Services.INSTANCE.get(
                NuminousDropTableService::class.java,
            )
        return if (args.isEmpty()) {
            dropTableService.dropTables
                .map { obj -> obj.id }
        } else {
            dropTableService.dropTables
                .map { obj -> obj.id }
                .filter { id: String ->
                    id.lowercase()
                        .startsWith(args.joinToString(" ").lowercase())
                }
        }
    }
}
