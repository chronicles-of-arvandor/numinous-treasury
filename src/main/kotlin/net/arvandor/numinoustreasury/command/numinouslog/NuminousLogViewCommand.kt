package net.arvandor.numinoustreasury.command.numinouslog

import com.rpkit.core.bukkit.pagination.PaginatedView
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class NuminousLogViewCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.numinouslog.view")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to view item logs.")
            return true
        }

        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to perform this command.")
            return true
        }

        val itemInHand: ItemStack = sender.inventory.itemInMainHand

        val numinousItem: NuminousItemStack? = NuminousItemStack.fromItemStack(itemInHand)
        if (numinousItem == null) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be holding a Numinous Treasury item to perform this command.")
            return true
        }

        if (!numinousItem.itemType.isAllowLogEntries) {
            sender.sendMessage(ChatColor.RED.toString() + "This item does not allow log entries.")
            return true
        }

        val logEntries = numinousItem.logEntries
        if (logEntries?.isEmpty() == true) {
            sender.sendMessage(ChatColor.RED.toString() + "This item has no log entries.")
            return true
        }

        var page = 1
        if (args.size > 0) {
            try {
                page = args[0].toInt()
            } catch (exception: NumberFormatException) {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /numinouslog view [page]")
                return true
            }
        }

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)

        val systemPrefix = TextComponent("[System] ")
        systemPrefix.color = ChatColor.BLUE
        systemPrefix.hoverEvent =
            HoverEvent(
                SHOW_TEXT,
                Text(
                    "This entry was created by a system action.",
                ),
            )

        val userPrefix = TextComponent("[User] ")
        userPrefix.color = ChatColor.AQUA
        userPrefix.hoverEvent =
            HoverEvent(
                SHOW_TEXT,
                Text(
                    "This entry was created manually by a user.",
                ),
            )

        val view =
            PaginatedView.fromChatComponents(
                TextComponent.fromLegacyText(
                    ChatColor.GRAY.toString() + "=== " + ChatColor.WHITE + "Log entries" + ChatColor.GRAY + " ===",
                ),
                logEntries
                    ?.sortedWith { a, b -> b.createdAt.compareTo(a.createdAt) }
                    ?.flatMap { logEntry ->
                        if (logEntry.minecraftUuid != null) {
                            return@flatMap listOf(
                                TextComponent.fromLegacyText(
                                    ChatColor.GRAY.toString() + dateTimeFormatter.format(logEntry.createdAt) + " " +
                                        ChatColor.YELLOW + plugin.server.getOfflinePlayer(logEntry.minecraftUuid).name,
                                ),
                                arrayOf(if (logEntry.isSystem) systemPrefix else userPrefix),
                                logEntry.text,
                            )
                        } else {
                            return@flatMap listOf(
                                TextComponent.fromLegacyText(ChatColor.GRAY.toString() + dateTimeFormatter.format(logEntry.createdAt)),
                                arrayOf(if (logEntry.isSystem) systemPrefix else userPrefix),
                                logEntry.text,
                            )
                        }
                    },
                ChatColor.GREEN.toString() + "< Previous",
                "Click here to view the previous page",
                ChatColor.GREEN.toString() + "Next >",
                "Click here to view the next page",
                { pageNumber: Int -> "Page $pageNumber" },
                10,
                { pageNumber: Int -> "/numinouslog view $pageNumber" },
            )

        if (view.isPageValid(page)) {
            view.sendPage(sender, page)
            val addLogEntry = TextComponent("Click here to add a log entry")
            addLogEntry.color = ChatColor.GREEN
            addLogEntry.clickEvent =
                ClickEvent(
                    RUN_COMMAND,
                    "/numinouslog add",
                )
            addLogEntry.hoverEvent =
                HoverEvent(
                    SHOW_TEXT,
                    Text(
                        "Click here to add a log entry to this item.",
                    ),
                )
            sender.spigot().sendMessage(addLogEntry)
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid page number.")
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
