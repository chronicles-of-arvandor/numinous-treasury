package net.arvandor.numinoustreasury.command.numinouslog;

import static java.time.ZoneOffset.UTC;
import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

import com.rpkit.core.bukkit.pagination.PaginatedView;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class NuminousLogViewCommand implements CommandExecutor, TabCompleter {
        private final NuminousTreasury plugin;

        public NuminousLogViewCommand(NuminousTreasury plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("numinoustreasury.command.numinouslog.view")) {
                sender.sendMessage(RED + "You do not have permission to view item logs.");
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(RED + "You must be a player to perform this command.");
                return true;
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            NuminousItemStack numinousItem = NuminousItemStack.fromItemStack(itemInHand);
            if (numinousItem == null) {
                sender.sendMessage(RED + "You must be holding a Numinous Treasury item to perform this command.");
                return true;
            }

            if (!numinousItem.getItemType().isAllowLogEntries()) {
                sender.sendMessage(RED + "This item does not allow log entries.");
                return true;
            }

            List<NuminousLogEntry> logEntries = numinousItem.getLogEntries();
            if (logEntries.isEmpty()) {
                sender.sendMessage(RED + "This item has no log entries.");
                return true;
            }

            int page = 1;
            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(RED + "Usage: /numinouslog view [page]");
                    return true;
                }
            }

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(UTC);

            TextComponent systemPrefix = new TextComponent("[System] ");
            systemPrefix.setColor(BLUE);
            systemPrefix.setHoverEvent(new HoverEvent(
                    SHOW_TEXT,
                    new Text(
                            "This entry was created by a system action."
                    )
            ));

            TextComponent userPrefix = new TextComponent("[User] ");
            userPrefix.setColor(AQUA);
            userPrefix.setHoverEvent(new HoverEvent(
                    SHOW_TEXT,
                    new Text(
                            "This entry was created manually by a user."
                    )
            ));

            PaginatedView view = PaginatedView.fromChatComponents(
                    TextComponent.fromLegacyText(GRAY + "=== " + WHITE + "Log entries" + GRAY + " ==="),
                    logEntries.stream()
                            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                            .flatMap(logEntry -> {
                                if  (logEntry.getMinecraftUuid() != null) {
                                    return Stream.of(
                                            TextComponent.fromLegacyText(
                                                    GRAY + dateTimeFormatter.format(logEntry.getCreatedAt()) + " " +
                                                            YELLOW + plugin.getServer().getOfflinePlayer(logEntry.getMinecraftUuid()).getName()
                                            ),
                                            Stream.concat(
                                                    Stream.of(logEntry.isSystem() ? systemPrefix : userPrefix),
                                                    Arrays.stream(logEntry.getText())
                                            ).toArray(BaseComponent[]::new)
                                    );
                                } else {
                                    return Stream.of(
                                            TextComponent.fromLegacyText(GRAY + dateTimeFormatter.format(logEntry.getCreatedAt())),
                                            Stream.concat(
                                                    Stream.of(logEntry.isSystem() ? systemPrefix : userPrefix),
                                                    Arrays.stream(logEntry.getText())
                                            ).toArray(BaseComponent[]::new)
                                    );
                                }
                            }).toList(),
                    GREEN + "< Previous",
                    "Click here to view the previous page",
                    GREEN + "Next >",
                    "Click here to view the next page",
                    (pageNumber) -> "Page " + pageNumber,
                    10,
                    (pageNumber) -> "/numinouslog view " + pageNumber
            );

            if (view.isPageValid(page)) {
                view.sendPage(sender, page);
                TextComponent addLogEntry = new TextComponent("Click here to add a log entry");
                addLogEntry.setColor(GREEN);
                addLogEntry.setClickEvent(new ClickEvent(
                        RUN_COMMAND,
                        "/numinouslog add"
                ));
                addLogEntry.setHoverEvent(new HoverEvent(
                        SHOW_TEXT,
                        new Text(
                                "Click here to add a log entry to this item."
                        )
                ));
                sender.spigot().sendMessage(addLogEntry);
            } else {
                sender.sendMessage(RED + "Invalid page number.");
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return List.of();
        }
}
