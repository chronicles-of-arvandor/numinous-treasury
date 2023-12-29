package net.arvandor.numinoustreasury.command.node.session;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

import com.rpkit.core.bukkit.pagination.PaginatedView;
import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.droptable.NuminousDropTable;
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService;
import net.arvandor.numinoustreasury.node.NuminousNodeCreationSession;
import net.arvandor.numinoustreasury.node.NuminousNodeService;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        boolean dropTableSpecified = args.length > 1 && args[0].equalsIgnoreCase("set");
        int page = 1;
        if (args.length > 1 && args[0].equalsIgnoreCase("page")) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(RED + "Page number must be an integer.");
                return true;
            }
        }
        if (dropTableSpecified) {
            String dropTableId = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
            NuminousDropTable dropTable = dropTableService.getDropTableById(dropTableId);
            if (dropTable == null) {
                sender.sendMessage(RED + "There is no drop table with that ID.");
                return true;
            }
            session.setDropTable(dropTable);
            session.display(player);
        } else {
            List<NuminousDropTable> dropTables = dropTableService.getDropTables();
            if (dropTables.isEmpty()) {
                sender.sendMessage(RED + "There are no drop tables configured.");
                return true;
            }
            TextComponent title = new TextComponent("Select a drop table: ");
            title.setColor(WHITE);
            PaginatedView view = PaginatedView.fromChatComponents(
                    new BaseComponent[] {
                            title
                    },
                    dropTables.stream().map(dropTable -> new ComponentBuilder(dropTable.getId())
                            .color(GREEN)
                            .event(new ClickEvent(
                                    RUN_COMMAND,
                                    "/node session droptable set " + dropTable.getId()
                            ))
                            .event(new HoverEvent(
                                    SHOW_TEXT,
                                    new Text(
                                            new ComponentBuilder("Click here to set the drop table to " + dropTable.getId())
                                                    .color(GRAY)
                                                    .create()
                                    )
                            ))
                            .create()).toList(),
                    GREEN + "< Previous",
                    "Click here to view the previous page",
                    GREEN + "Next >",
                    "Click here to view the next page",
                    (pageNumber) -> "Page " + pageNumber,
                    10,
                    (pageNumber) -> "/node session droptable page " + pageNumber
            );
            if (view.isPageValid(page)) {
                view.sendPage(sender, page);
            } else {
                sender.sendMessage(RED + "Invalid page number.");
            }
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
