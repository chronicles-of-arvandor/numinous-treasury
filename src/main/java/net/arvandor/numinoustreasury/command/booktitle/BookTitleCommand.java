package net.arvandor.numinoustreasury.command.booktitle;

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;
import static org.bukkit.Material.WRITTEN_BOOK;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class BookTitleCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != WRITTEN_BOOK) {
            sender.sendMessage(RED + "You must be holding a written or writable book to use this command.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage(RED + "Your held item does not have item meta.");
            return true;
        }

        if (!(meta instanceof BookMeta bookMeta)) {
            sender.sendMessage(RED + "Your held item does not have book meta.");
            return true;
        }

        String title = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        bookMeta.setTitle(title.substring(0, 32));
        bookMeta.setDisplayName(title);
        item.setItemMeta(bookMeta);
        player.getInventory().setItemInMainHand(item);
        sender.sendMessage(GREEN + "Title updated.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
