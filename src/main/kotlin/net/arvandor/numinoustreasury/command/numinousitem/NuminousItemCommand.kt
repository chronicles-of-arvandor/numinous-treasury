package net.arvandor.numinoustreasury.command.numinousitem

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.NuminousItemService
import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.arvandor.numinoustreasury.mixpanel.NuminousMixpanelService
import net.arvandor.numinoustreasury.mixpanel.event.NuminousMixpanelItemCreatedEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.time.Instant

class NuminousItemCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to create items.")
            return true
        }
        if (!sender.hasPermission("numinoustreasury.command.numinousitem")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to create items.")
            return true
        }
        if (args.size < 1) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /" + label + " [item type] (amount)")
        }
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        var itemName = args.joinToString(" ")
        var amount = 1
        var itemType = itemService.getItemTypeById(itemName)
        if (itemType == null) {
            itemType = itemService.getItemTypeByName(itemName)
        }
        if (itemType == null && args.size > 1) {
            itemName = args.dropLast(1).joinToString(" ")
            itemType = itemService.getItemTypeById(itemName)
            if (itemType == null) {
                itemType = itemService.getItemTypeByName(itemName)
            }
            try {
                amount = args.last().toInt()
            } catch (exception: NumberFormatException) {
                itemType = null
            }
        }
        if (itemType == null) {
            sender.sendMessage(ChatColor.RED.toString() + "There is no item by that name.")
            return true
        }
        val numinousItem =
            NuminousItemStack(
                itemType,
                amount,
                null,
                listOf(
                    NuminousLogEntry(
                        Instant.now(),
                        sender.uniqueId,
                        true,
                        arrayOf<BaseComponent>(
                            TextComponent("Created via command"),
                        ),
                    ),
                ),
            )
        val bukkitItem = numinousItem.toItemStack()
        sender.inventory.addItem(bukkitItem).values.forEach { overflowItem ->
            sender.world.dropItem(
                sender.location,
                overflowItem,
            )
        }
        sender.sendMessage(ChatColor.GREEN.toString() + "Created " + amount + " × " + itemType.name)

        val mixpanelService =
            Services.INSTANCE.get(
                NuminousMixpanelService::class.java,
            )
        val finalItemType = itemType
        val finalAmount = amount
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                mixpanelService.trackEvent(NuminousMixpanelItemCreatedEvent(sender, finalItemType, finalAmount, "Command"))
            },
        )
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        return if (args.isEmpty()) {
            itemService.itemTypes.flatMap { item ->
                listOf(
                    item.id,
                    item.name,
                )
            }
        } else {
            itemService.itemTypes.flatMap { item ->
                listOf(
                    item.id,
                    item.name,
                )
            }.filter { name: String ->
                name.lowercase()
                    .startsWith(args.joinToString(" ").lowercase())
            }
        }
    }
}
