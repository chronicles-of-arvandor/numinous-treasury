package net.arvandor.numinoustreasury.command.node.session;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public final class NodeSessionProfessionAddCommand implements CommandExecutor, TabCompleter {

    private final ConversationFactory conversationFactory;

    public NodeSessionProfessionAddCommand(NuminousTreasury plugin) {
        conversationFactory = new ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(new LevelPrompt())
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
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        if (args.length > 0) {
            int level;
            try {
                level = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException exception) {
                String professionName = String.join(" ", args);
                NuminousProfession profession = professionService.getProfessionById(professionName);
                if (profession == null) {
                    profession = professionService.getProfessionByName(professionName);
                }
                if (profession == null) {
                    sender.sendMessage(RED + "There is no profession by that name.");
                    return true;
                }
                Conversation conversation = conversationFactory.buildConversation(player);
                conversation.getContext().setSessionData("profession", profession);
                conversation.begin();
                return true;
            }


            String professionName = Arrays.stream(args).limit(args.length - 1)
                    .collect(Collectors.joining(" "));
            NuminousProfession profession = professionService.getProfessionById(professionName);
            if (profession == null) {
                profession = professionService.getProfessionByName(professionName);
            }
            if (profession == null) {
                sender.sendMessage(RED + "There is no profession by that name.");
                return true;
            }
            session.addRequiredProfessionLevel(profession, level);
            session.display(player);
        } else {
            player.sendMessage(WHITE + "Select a profession: ");
            professionService.getProfessions().forEach(profession -> {
                player.spigot().sendMessage(
                        new ComponentBuilder(profession.getName())
                                .color(GRAY)
                                .event(new ClickEvent(
                                        RUN_COMMAND,
                                        "/node session profession add " + profession.getId()
                                ))
                                .event(new HoverEvent(
                                        SHOW_TEXT,
                                        new Text(
                                                new ComponentBuilder("Click here to select " + profession.getName())
                                                        .color(GRAY)
                                                        .create()
                                        )
                                ))
                                .create()
                );
            });
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        if (args.length == 0) {
            return professionService.getProfessions().stream()
                    .flatMap(profession -> Stream.of(
                            profession.getId(),
                            profession.getName()))
                    .toList();
        } else {
            return professionService.getProfessions().stream()
                    .flatMap(profession -> Stream.of(
                            profession.getId(),
                            profession.getName())
                    )
                    .filter(name -> name.toLowerCase().startsWith(String.join(" ", args).toLowerCase()))
                    .toList();
        }
    }

    private static class LevelPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return GRAY + "Enter the level of the profession that should be required:";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            NuminousProfession profession = (NuminousProfession) context.getSessionData("profession");
            Conversable forWhom = context.getForWhom();
            if (forWhom instanceof Player player) {
                player.performCommand("node session profession add " + profession.getId() + " " + input.intValue());
            }
            return END_OF_CONVERSATION;
        }

    }
}
