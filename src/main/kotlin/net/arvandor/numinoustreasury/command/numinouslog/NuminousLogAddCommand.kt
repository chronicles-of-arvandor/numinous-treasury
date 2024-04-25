package net.arvandor.numinoustreasury.command.numinouslog

import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.NuminousItemStack
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.time.Instant

class NuminousLogAddCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory =
            ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(TextPrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(ChatColor.RED.toString() + "You must be a player to add log entries.")
                .addConversationAbandonedListener { event: ConversationAbandonedEvent ->
                    if (!event.gracefulExit()) {
                        val forWhom = event.context.forWhom
                        if (forWhom is Player) {
                            forWhom.sendMessage(ChatColor.RED.toString() + "Operation cancelled.")
                        }
                    }
                }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.numinouslog.add")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to add log entries.")
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

        val logEntries: MutableList<NuminousLogEntry> = ArrayList(numinousItem.logEntries)

        if (args.size < 1) {
            if (sender.isConversing) {
                sender.sendMessage(ChatColor.RED.toString() + "Please finish your current action before trying to add a log entry.")
                return true
            }
            val conversation = conversationFactory.buildConversation(sender)
            conversation.context.setSessionData("item", itemInHand)
            conversation.begin()
            return true
        }

        logEntries.add(
            NuminousLogEntry(
                Instant.now(),
                sender.uniqueId,
                false,
                arrayOf<BaseComponent>(
                    TextComponent(args.joinToString(" ")),
                ),
            ),
        )

        val meta = itemInHand.itemMeta
        if (meta != null) {
            meta.persistentDataContainer.set<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                plugin.keys.logEntries,
                PersistentDataType.TAG_CONTAINER_ARRAY,
                logEntries
                    .map { entry ->
                        entry.toCompoundTag(
                            plugin,
                            meta.persistentDataContainer,
                        )
                    }
                    .toTypedArray(),
            )
            itemInHand.setItemMeta(meta)
            sender.sendMessage(ChatColor.GREEN.toString() + "Added log entry.")
            sender.performCommand("numinouslog view")
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Failed to add log entry.")
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

    private inner class TextPrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.GRAY.toString() + "Please enter the text of the log entry:"
        }

        override fun acceptInput(
            context: ConversationContext,
            input: String?,
        ): Prompt? {
            val item = context.getSessionData("item") as ItemStack?
            val numinousItem: NuminousItemStack? = NuminousItemStack.fromItemStack(item)
            if (numinousItem == null) {
                return ErrorPrompt("You must be holding a Numinous Treasury item to perform this command.")
            }
            val logEntries = numinousItem.logEntries?.toMutableList()
            if (logEntries == null) {
                return ErrorPrompt("This item does not allow log entries.")
            }
            val player = context.forWhom as Player
            logEntries.add(
                NuminousLogEntry(
                    Instant.now(),
                    player.uniqueId,
                    false,
                    TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes('&', input),
                    ),
                ),
            )
            val meta = item!!.itemMeta
            if (meta != null) {
                meta.persistentDataContainer.set<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                    plugin.keys.logEntries,
                    PersistentDataType.TAG_CONTAINER_ARRAY,
                    logEntries
                        .map { entry ->
                            entry.toCompoundTag(
                                plugin,
                                meta.persistentDataContainer,
                            )
                        }
                        .toTypedArray(),
                )
                item.setItemMeta(meta)
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        player.sendMessage(ChatColor.GREEN.toString() + "Added log entry.")
                        player.performCommand("numinouslog view")
                    },
                )
            } else {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        player.sendMessage(ChatColor.RED.toString() + "Failed to add log entry.")
                    },
                )
            }
            return END_OF_CONVERSATION
        }
    }

    private inner class ErrorPrompt(private val message: String) : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.RED.toString() + message
        }

        override fun acceptInput(
            context: ConversationContext,
            input: String?,
        ): Prompt? {
            return END_OF_CONVERSATION
        }
    }
}
