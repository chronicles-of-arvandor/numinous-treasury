package net.kingdommc.darkages.numinoustreasury.command.numinousitem;

import com.rpkit.core.service.Services;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemService;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemStack;
import net.kingdommc.darkages.numinoustreasury.item.NuminousItemType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;

public final class NuminousItemCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to create items.");
            return true;
        }
        if (!sender.hasPermission("numinoustreasury.command.numinousitem")) {
            sender.sendMessage(RED + "You do not have permission to create items.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /" + label + " [item type] (amount)");
        }
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        String itemName = String.join(" ", args);
        int amount = 1;
        NuminousItemType itemType = itemService.getItemTypeById(itemName);
        if (itemType == null) {
            itemType = itemService.getItemTypeByName(itemName);
        }
        if (itemType == null && args.length > 1) {
            itemName = String.join(" ", Arrays.stream(args).limit(args.length - 1).toArray(String[]::new));
            itemType = itemService.getItemTypeById(itemName);
            if (itemType == null) {
                itemType = itemService.getItemTypeByName(itemName);
            }
            try {
                amount = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException exception) {
                itemType = null;
            }
        }
        if (itemType == null) {
            sender.sendMessage(RED + "There is no item by that name.");
            return true;
        }
        NuminousItemStack numinousItem = new NuminousItemStack(itemType, amount);
        ItemStack bukkitItem = numinousItem.toItemStack();
        player.getInventory().addItem(bukkitItem).values().forEach(overflowItem -> player.getWorld().dropItem(player.getLocation(), overflowItem));
        sender.sendMessage(GREEN + "Created " + amount + " × " + itemType.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        if (args.length == 0) {
            return itemService.getItemTypes().stream().flatMap(item -> Stream.of(item.getId(), item.getName())).toList();
        } else {
            return itemService.getItemTypes().stream().flatMap(item -> Stream.of(item.getId(), item.getName())).filter(name -> name.toLowerCase().startsWith(String.join(" ", args).toLowerCase())).toList();
        }
    }
}
