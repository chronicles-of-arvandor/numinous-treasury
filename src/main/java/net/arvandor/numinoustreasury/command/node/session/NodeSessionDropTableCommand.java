package net.arvandor.numinoustreasury.command.node.session;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.droptable.NuminousDropTable;
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService;
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
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

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public final class NodeSessionDropTableCommand implements CommandExecutor, TabCompleter {

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
        NuminousDropTableService dropTableService = Services.INSTANCE.get(NuminousDropTableService.class);
        if (args.length > 0) {
            String dropTableId = String.join(" ", args);
            NuminousDropTable dropTable = dropTableService.getDropTableById(dropTableId);
            if (dropTable == null) {
                sender.sendMessage(RED + "There is no drop table with that ID.");
                return true;
            }
            session.setDropTable(dropTable);
            session.display(player);
        } else {
            player.sendMessage(WHITE + "Select a drop table: ");
            dropTableService.getDropTables().forEach(dropTable -> {
                player.spigot().sendMessage(
                        new ComponentBuilder(dropTable.getId())
                                .color(GRAY)
                                .event(new ClickEvent(
                                        RUN_COMMAND,
                                        "/node session droptable " + dropTable.getId()
                                ))
                                .event(new HoverEvent(
                                        SHOW_TEXT,
                                        new Text(
                                                new ComponentBuilder("Click here to set the drop table to " + dropTable.getId())
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
        NuminousDropTableService dropTableService = Services.INSTANCE.get(NuminousDropTableService.class);
        if (args.length == 0) {
            return dropTableService.getDropTables().stream()
                    .map(NuminousDropTable::getId)
                    .toList();
        } else {
            return dropTableService.getDropTables().stream()
                    .map(NuminousDropTable::getId)
                    .filter(id -> id.toLowerCase().startsWith(String.join(" ", args).toLowerCase()))
                    .toList();
        }
    }
}
