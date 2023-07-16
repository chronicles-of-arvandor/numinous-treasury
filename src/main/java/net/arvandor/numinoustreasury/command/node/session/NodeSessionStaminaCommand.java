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

import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.ChatColor.RED;

public final class NodeSessionStaminaCommand implements CommandExecutor, TabCompleter {

    private final ConversationFactory conversationFactory;

    public NodeSessionStaminaCommand(NuminousTreasury plugin) {
        conversationFactory = new ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(new ExperiencePrompt())
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
            int experience;
            try {
                experience = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(RED + "Stamina must be an integer.");
                return true;
            }
            session.setStaminaCost(experience);
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

    private static class ExperiencePrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return GRAY + "Please enter the stamina cost when using the node:";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            NuminousNodeCreationSession session = (NuminousNodeCreationSession) context.getSessionData("session");
            session.setStaminaCost(input.intValue());
            Conversable forWhom = context.getForWhom();
            if (forWhom instanceof Player player) {
                session.display(player);
            }
            return END_OF_CONVERSATION;
        }
    }

}
