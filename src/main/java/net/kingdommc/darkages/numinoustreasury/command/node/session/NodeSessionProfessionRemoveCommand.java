package net.kingdommc.darkages.numinoustreasury.command.node.session;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.node.NuminousNodeCreationSession;
import net.kingdommc.darkages.numinoustreasury.node.NuminousNodeService;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfession;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfessionService;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public final class NodeSessionProfessionRemoveCommand implements CommandExecutor, TabCompleter {

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
            String professionName = String.join(" ", args);
            NuminousProfession profession = professionService.getProfessionById(professionName);
            if (profession == null) {
                profession = professionService.getProfessionByName(professionName);
            }
            if (profession == null) {
                sender.sendMessage(RED + "There is no profession by that name.");
                return true;
            }
            session.removeRequiredProfessionLevel(profession);
            session.display(player);
        } else {
            player.sendMessage(WHITE + "Select a profession: ");
            professionService.getProfessions().forEach(profession -> {
                player.spigot().sendMessage(
                        new ComponentBuilder(profession.getName())
                                .color(GRAY)
                                .event(new ClickEvent(
                                        RUN_COMMAND,
                                        "/node session profession remove " + profession.getId()
                                ))
                                .event(new HoverEvent(
                                        SHOW_TEXT,
                                        new Text(
                                                new ComponentBuilder("Click here to remove " + profession.getName())
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
}
