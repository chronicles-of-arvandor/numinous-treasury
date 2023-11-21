package net.arvandor.numinoustreasury.command.numinouslog;

import static net.md_5.bungee.api.ChatColor.*;

import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.NuminousItemStack;
import net.arvandor.numinoustreasury.item.log.NuminousLogEntry;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class NuminousLogAddCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;
    private final ConversationFactory conversationFactory;

    public NuminousLogAddCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(false)
                .withFirstPrompt(new TextPrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(RED + "You must be a player to add log entries.")
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
        if (!sender.hasPermission("numinoustreasury.command.numinouslog.add")) {
            sender.sendMessage(RED + "You do not have permission to add log entries.");
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

        List<NuminousLogEntry> logEntries = new ArrayList<>(numinousItem.getLogEntries());

        if (args.length < 1) {
            if (player.isConversing()) {
                sender.sendMessage(RED + "Please finish your current action before trying to add a log entry.");
                return true;
            }
            Conversation conversation = conversationFactory.buildConversation(player);
            conversation.getContext().setSessionData("item", itemInHand);
            conversation.begin();
            return true;
        }

        logEntries.add(new NuminousLogEntry(
                Instant.now(),
                player.getUniqueId(),
                false,
                new BaseComponent[] {
                        new TextComponent(String.join(" ", args))
                }
        ));

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(
                    plugin.keys().logEntries(),
                    PersistentDataType.TAG_CONTAINER_ARRAY,
                    logEntries.stream()
                            .map(entry -> entry.toCompoundTag(plugin, meta.getPersistentDataContainer()))
                            .toArray(PersistentDataContainer[]::new)
            );
            itemInHand.setItemMeta(meta);
            sender.sendMessage(GREEN + "Added log entry.");
            player.performCommand("numinouslog view");
        } else {
            sender.sendMessage(RED + "Failed to add log entry.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

    private class TextPrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return GRAY + "Please enter the text of the log entry:";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            ItemStack item = (ItemStack) context.getSessionData("item");
            NuminousItemStack numinousItem = NuminousItemStack.fromItemStack(item);
            List<NuminousLogEntry> logEntries = new ArrayList<>(numinousItem.getLogEntries());
            Player player = (Player) context.getForWhom();
            logEntries.add(new NuminousLogEntry(
                    Instant.now(),
                    player.getUniqueId(),
                    false,
                    TextComponent.fromLegacyText(
                            ChatColor.translateAlternateColorCodes('&', input)
                    )
            ));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(
                        plugin.keys().logEntries(),
                        PersistentDataType.TAG_CONTAINER_ARRAY,
                        logEntries.stream()
                                .map(entry -> entry.toCompoundTag(plugin, meta.getPersistentDataContainer()))
                                .toArray(PersistentDataContainer[]::new)
                );
                item.setItemMeta(meta);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(GREEN + "Added log entry.");
                    player.performCommand("numinouslog view");
                });
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(RED + "Failed to add log entry.");
                });
            }
            return END_OF_CONVERSATION;
        }
    }
}
