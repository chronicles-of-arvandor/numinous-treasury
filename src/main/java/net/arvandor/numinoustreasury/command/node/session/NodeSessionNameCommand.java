package net.arvandor.numinoustreasury.command.node.session;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;

public final class NodeSessionNameCommand implements CommandExecutor, TabCompleter {

    private final ConversationFactory conversationFactory;

    public NodeSessionNameCommand(NuminousTreasury plugin) {
        conversationFactory = new ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(new NamePrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(RED + "You must be a player to create nodes.")
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        Conversable forWhom = event.getContext().getForWhom();
                        if (forWhom instanceof Player player) {
                            player.sendMessage(RED + "Operation cancelled.");
                        }
                    }
                });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.node.create")) {
            sender.sendMessage(RED + "You do not have permission to create nodes.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to create nodes.");
            return true;
        }
        NuminousNodeService nodeService = Services.INSTANCE.get(NuminousNodeService.class);
        NuminousNodeCreationSession session = nodeService.getNodeCreationSession(player);
        if (session == null) {
            session = new NuminousNodeCreationSession();
            nodeService.setNodeCreationSession(player, session);
        }
        if (args.length > 0) {
            session.setName(String.join(" ", args));
            session.display(player);
        } else {
            Conversation conversation = conversationFactory.buildConversation(player);
            conversation.getContext().setSessionData("session", session);
            conversation.begin();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

    private static class NamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return GRAY + "Please enter the name of the node:";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            NuminousNodeCreationSession session = (NuminousNodeCreationSession) context.getSessionData("session");
            session.setName(input);
            Conversable forWhom = context.getForWhom();
            if (forWhom instanceof Player player) {
                session.display(player);
            }
            return END_OF_CONVERSATION;
        }

    }

}
