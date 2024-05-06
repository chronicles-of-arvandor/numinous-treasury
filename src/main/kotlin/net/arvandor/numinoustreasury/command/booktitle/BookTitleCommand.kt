package net.arvandor.numinoustreasury.command.booktitle

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material.WRITTEN_BOOK
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

class BookTitleCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this command.")
            return true
        }

        val item: ItemStack = sender.inventory.itemInMainHand
        if (item.type != WRITTEN_BOOK) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be holding a written or writable book to use this command.")
            return true
        }

        val meta = item.itemMeta
        if (meta == null) {
            sender.sendMessage(ChatColor.RED.toString() + "Your held item does not have item meta.")
            return true
        }

        if (meta !is BookMeta) {
            sender.sendMessage(ChatColor.RED.toString() + "Your held item does not have book meta.")
            return true
        }

        val title = ChatColor.translateAlternateColorCodes('&', args.joinToString(" "))
        if (title.length > 32) {
            meta.setTitle(title.substring(0, 32))
        } else {
            meta.setTitle(title)
        }
        meta.setDisplayName(title)
        item.setItemMeta(meta)
        sender.inventory.setItemInMainHand(item)
        sender.sendMessage(ChatColor.GREEN.toString() + "Title updated.")
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
