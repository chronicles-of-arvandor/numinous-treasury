package net.arvandor.numinoustreasury.command.node.session

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession
import net.arvandor.numinoustreasury.node.NuminousNodeService
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.NumericPrompt
import org.bukkit.conversations.Prompt
import org.bukkit.entity.Player

class NodeSessionStaminaCommand(plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory =
            ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(ExperiencePrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(ChatColor.RED.toString() + "You must be a player to create nodes.")
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
        if (args.size > 0) {
            val experience: Int
            try {
                experience = args[0].toInt()
            } catch (exception: NumberFormatException) {
                sender.sendMessage(ChatColor.RED.toString() + "Stamina must be an integer.")
                return true
            }
            session.staminaCost = experience
            session.display(sender)
        } else {
            val conversation = conversationFactory.buildConversation(sender)
            conversation.context.setSessionData("session", session)
            conversation.begin()
        }
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

    private class ExperiencePrompt : NumericPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.GRAY.toString() + "Please enter the stamina cost when using the node:"
        }

        override fun acceptValidatedInput(
            context: ConversationContext,
            input: Number,
        ): Prompt? {
            val session = context.getSessionData("session") as NuminousNodeCreationSession
            session.staminaCost = input.toInt()
            val forWhom = context.forWhom
            if (forWhom is Player) {
                session.display(forWhom)
            }
            return END_OF_CONVERSATION
        }
    }
}
