package net.arvandor.numinoustreasury.command.numinousmodify;

import static java.util.logging.Level.SEVERE;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

import com.rpkit.core.bukkit.pagination.PaginatedView;
import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.item.NuminousItemCategory;
import net.arvandor.numinoustreasury.item.NuminousItemService;
import net.arvandor.numinoustreasury.item.NuminousItemType;
import net.arvandor.numinoustreasury.item.NuminousRarity;
import net.arvandor.numinoustreasury.measurement.Weight;
import net.arvandor.numinoustreasury.measurement.WeightUnit;
import net.arvandor.numinoustreasury.utils.Args;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NuminousModifyCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    public NuminousModifyCommand(NuminousTreasury plugin) {
        this.plugin = plugin;

        this.updateNameFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new NamePrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        event.getContext().getForWhom().sendRawMessage(RED + "Operation cancelled.");
                    }
                });

        this.updateWeightFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new WeightPrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        event.getContext().getForWhom().sendRawMessage(RED + "Operation cancelled.");
                    }
                });

        this.updateInventorySlotsFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new InventorySlotsPrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        event.getContext().getForWhom().sendRawMessage(RED + "Operation cancelled.");
                    }
                });

        this.updateLoreFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new LorePrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        event.getContext().getForWhom().sendRawMessage(RED + "Operation cancelled.");
                    }
                });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.numinousmodify")) {
            sender.sendMessage(RED + "You do not have permission to modify numinous items.");
            return true;
        }
        String[] unquotedArgs = Args.unquote(args);

        if (unquotedArgs.length < 1) {
            sender.sendMessage(RED + "Usage: /numinousmodify [item id] (action)");
            return true;
        }

        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);

        String itemId = unquotedArgs[0];
        NuminousItemType itemType = itemService.getItemTypeById(itemId);
        if (itemType == null) {
            itemType = itemService.getItemTypeByName(itemId);
        }
        if (itemType == null) {
            sender.sendMessage(RED + "No item type found with id or name " + itemId);
            return true;
        }

        int page = -1;
        if (unquotedArgs.length >= 2) {
            try {
                page = Integer.parseInt(unquotedArgs[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (unquotedArgs.length < 2 || page != -1) {
            if (page == -1) {
                page = 1;
            }
            TextComponent itemName = new TextComponent(itemType.getName() + " ");
            itemName.setColor(ChatColor.WHITE);
            TextComponent editNameButton = new TextComponent("(Edit)");
            editNameButton.setColor(GREEN);
            editNameButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " name"));
            editNameButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to edit the name.")));
            BaseComponent[] title = new BaseComponent[]{
                    itemName,
                    editNameButton
            };
            TextComponent categoriesHeader = new TextComponent("Categories");
            categoriesHeader.setColor(ChatColor.WHITE);
            categoriesHeader.setBold(true);
            TextComponent addCategoryButton = new TextComponent("+");
            addCategoryButton.setColor(GREEN);
            addCategoryButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " categories add"));
            addCategoryButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add a category.")));
            NuminousItemType finalItemType = itemType;
            Stream<BaseComponent[]> categoriesLines = Stream.of(
                    Stream.<BaseComponent[]>of(new BaseComponent[]{categoriesHeader}),
                    itemType.getCategories().stream().map(category -> {
                        TextComponent removeButton = new TextComponent("-");
                        removeButton.setColor(RED);
                        removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " categories remove " + category.name()));
                        removeButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to remove this category.")));
                        TextComponent categoryName = new TextComponent(" " + category.name());
                        categoryName.setColor(ChatColor.WHITE);
                        return new BaseComponent[]{removeButton, categoryName};
                    }),
                    Stream.<BaseComponent[]>of(new BaseComponent[]{addCategoryButton})
            ).flatMap(Function.identity());

            TextComponent rarityTitle = new TextComponent("Rarity: ");
            rarityTitle.setColor(ChatColor.WHITE);
            rarityTitle.setBold(true);
            BaseComponent[] rarity = TextComponent.fromLegacyText(itemType.getRarity().getColor() + itemType.getRarity().getDisplayName());
            TextComponent editRarityButton = new TextComponent(" (Edit)");
            editRarityButton.setColor(GREEN);
            editRarityButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " rarity"));
            editRarityButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to edit the rarity.")));
            BaseComponent[] rarityLine = Stream.of(
                    Stream.of(new BaseComponent[] { rarityTitle }),
                    Stream.of(rarity),
                    Stream.of(new BaseComponent[] { editRarityButton })
            ).flatMap(Function.identity()).toArray(BaseComponent[]::new);

            TextComponent minecraftItemTitle = new TextComponent("Minecraft Item: ");
            minecraftItemTitle.setColor(ChatColor.WHITE);
            minecraftItemTitle.setBold(true);
            BaseComponent[] minecraftItem = TextComponent.fromLegacyText(itemType.getMinecraftItem().getType().name());
            TextComponent editMinecraftItemButton = new TextComponent(" (Update to held item)");
            editMinecraftItemButton.setColor(GREEN);
            editMinecraftItemButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " minecraftitem"));
            editMinecraftItemButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to update the Minecraft item to the item in your hand.")));
            BaseComponent[] minecraftItemLine = Stream.of(
                    Stream.of(new BaseComponent[] { minecraftItemTitle }),
                    Stream.of(minecraftItem),
                    Stream.of(new BaseComponent[] { editMinecraftItemButton })
            ).flatMap(Function.identity()).toArray(BaseComponent[]::new);

            TextComponent weightTitle = new TextComponent("Weight: ");
            weightTitle.setColor(ChatColor.WHITE);
            weightTitle.setBold(true);
            BaseComponent[] weight = TextComponent.fromLegacyText(itemType.getWeight().toString());
            TextComponent editWeightButton = new TextComponent(" (Edit)");
            editWeightButton.setColor(GREEN);
            editWeightButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " weight"));
            editWeightButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to edit the weight.")));
            BaseComponent[] weightLine = Stream.of(
                    Stream.of(new BaseComponent[] { weightTitle }),
                    Stream.of(weight),
                    Stream.of(new BaseComponent[] { editWeightButton })
            ).flatMap(Function.identity()).toArray(BaseComponent[]::new);

            TextComponent inventorySlotsTitle = new TextComponent("Inventory Slots: ");
            inventorySlotsTitle.setColor(ChatColor.WHITE);
            inventorySlotsTitle.setBold(true);
            BaseComponent[] inventorySlots = TextComponent.fromLegacyText(String.valueOf(itemType.getInventorySlots()));
            TextComponent editInventorySlotsButton = new TextComponent(" (Edit)");
            editInventorySlotsButton.setColor(GREEN);
            editInventorySlotsButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " inventoryslots"));
            editInventorySlotsButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to edit the inventory slots.")));
            BaseComponent[] inventorySlotsLine = Stream.of(
                    Stream.of(new BaseComponent[] { inventorySlotsTitle }),
                    Stream.of(inventorySlots),
                    Stream.of(new BaseComponent[] { editInventorySlotsButton })
            ).flatMap(Function.identity()).toArray(BaseComponent[]::new);

            TextComponent allowLogEntriesTitle = new TextComponent("Allow Log Entries: ");
            allowLogEntriesTitle.setColor(ChatColor.WHITE);
            allowLogEntriesTitle.setBold(true);
            BaseComponent[] allowLogEntries = TextComponent.fromLegacyText(String.valueOf(itemType.isAllowLogEntries()));
            TextComponent editAllowLogEntriesButton = new TextComponent(" (Toggle)");
            editAllowLogEntriesButton.setColor(GREEN);
            editAllowLogEntriesButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " allowlogentries " + !itemType.isAllowLogEntries()));
            editAllowLogEntriesButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to edit whether log entries are allowed.")));
            BaseComponent[] allowLogEntriesLine = Stream.of(
                    Stream.of(new BaseComponent[] { allowLogEntriesTitle }),
                    Stream.of(allowLogEntries),
                    Stream.of(new BaseComponent[] { editAllowLogEntriesButton })
            ).flatMap(Function.identity()).toArray(BaseComponent[]::new);

            ItemMeta meta = itemType.getMinecraftItem().getItemMeta();
            List<String> lore = null;
            if (meta != null) {
                lore = meta.getLore();
            }
            if (lore == null) {
                lore = new ArrayList<>();
            }
            TextComponent loreTitle = new TextComponent("Lore");
            loreTitle.setColor(ChatColor.WHITE);
            loreTitle.setBold(true);
            if (lore.isEmpty()) {
                TextComponent addButton = new TextComponent("+");
                addButton.setColor(GREEN);
                addButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore add 0"));
                addButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add a line of lore here.")));
                sender.spigot().sendMessage(addButton);
            }
            final List<String> finalLore = lore;
            Stream<BaseComponent[]> loreLines = IntStream.range(0, finalLore.size()).boxed().flatMap(lineNum -> {
                String line = finalLore.get(lineNum);
                TextComponent addButton1 = null;
                if (lineNum == 0) {
                    addButton1 = new TextComponent("+");
                    addButton1.setColor(GREEN);
                    addButton1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore add " + lineNum));
                    addButton1.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add a line of lore here.")));
                }
                TextComponent removeButton = new TextComponent("-");
                removeButton.setColor(RED);
                removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore remove " + lineNum));
                removeButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to remove this line of lore.")));
                TextComponent lineComponent = new TextComponent(" " + line);
                lineComponent.setColor(ChatColor.WHITE);
                TextComponent addButton2 = new TextComponent("+");
                addButton2.setColor(GREEN);
                addButton2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore add " + (lineNum + 1)));
                addButton2.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add a line of lore here.")));
                if (addButton1 == null) {
                    return Stream.of(
                            new BaseComponent[]{removeButton, lineComponent},
                            new BaseComponent[]{addButton2}
                    );
                } else {
                    return Stream.of(
                            new BaseComponent[]{addButton1},
                            new BaseComponent[]{removeButton, lineComponent},
                            new BaseComponent[]{addButton2}
                    );
                }
            });
            PaginatedView view = PaginatedView.fromChatComponents(
                    title,
                    Stream.of(
                            categoriesLines,
                            Stream.<BaseComponent[]>of(rarityLine),
                            Stream.<BaseComponent[]>of(minecraftItemLine),
                            Stream.<BaseComponent[]>of(weightLine),
                            Stream.<BaseComponent[]>of(inventorySlotsLine),
                            Stream.<BaseComponent[]>of(allowLogEntriesLine),
                            Stream.<BaseComponent[]>of(new BaseComponent[]{loreTitle}),
                            loreLines
                    ).flatMap(Function.identity()).toList(),
                    GREEN + "< Previous",
                    "Click here to view the previous page",
                    GREEN + "Next >",
                    "Click here to view the next page",
                    (pageNumber) -> "Page " + pageNumber,
                    10,
                    (pageNumber) -> "/numinousmodify " + finalItemType.getId() + " " + pageNumber
            );

            if (view.isPageValid(page)) {
                view.sendPage(sender, page);
            } else {
                sender.sendMessage(RED + "Invalid page number.");
            }
        } else {
            String action = unquotedArgs[1];
            if (action.equalsIgnoreCase("name")) {
                updateItemTypeName(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            } else if (action.equalsIgnoreCase("categories")) {
                updateItemTypeCategories(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            } else if (action.equalsIgnoreCase("rarity")) {
                updateItemTypeRarity(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            } else if (action.equalsIgnoreCase("minecraftitem")) {
                updateItemTypeMinecraftItem(sender, itemType);
            } else if (action.equalsIgnoreCase("weight")) {
                updateItemTypeWeight(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            } else if (action.equalsIgnoreCase("inventoryslots")) {
                updateItemTypeInventorySlots(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            } else if (action.equalsIgnoreCase("allowlogentries")) {
                updateItemTypeAllowLogEntries(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            } else if (action.equalsIgnoreCase("lore")) {
                updateItemTypeLore(sender, itemType, Arrays.stream(unquotedArgs).skip(2).toArray(String[]::new));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            return itemService.getItemTypes().stream()
                    .map(NuminousItemType::getId)
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            return List.of("name", "categories", "rarity", "minecraftitem", "weight", "inventoryslots", "allowlogentries", "lore");
        } else if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "categories":
                    return Arrays.stream(NuminousItemCategory.values())
                            .map(NuminousItemCategory::name)
                            .filter(category -> category.toLowerCase().startsWith(args[2].toLowerCase()))
                            .toList();
                case "rarity":
                    return Arrays.stream(NuminousRarity.values())
                            .map(NuminousRarity::name)
                            .filter(rarity -> rarity.toLowerCase().startsWith(args[2].toLowerCase()))
                            .toList();
                case "lore":
                    return Stream.of("add", "remove").filter(action -> action.toLowerCase().startsWith(args[2].toLowerCase()))
                            .toList();
                case "allowlogentries":
                    return Stream.of("true", "false").filter(value -> value.toLowerCase().startsWith(args[2].toLowerCase()))
                            .toList();
            }
        }
        return List.of();
    }

    private ConversationFactory updateNameFactory;

    private class NamePrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return "Enter the new name for the item type.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
            itemType.setName(input);
            try {
                itemService.save(itemType);
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                return new NameSetFailedPrompt();
            }
            return new NameSetPrompt();
        }
    }

    private class NameSetPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return GREEN + "Name set.";
        }
    }

    private class NameSetFailedPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return RED + "An error occured while setting the name.";
        }
    }

    private void updateItemTypeName(CommandSender sender, NuminousItemType itemType, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Conversable conversable)) {
                sender.sendMessage(RED + "You must specify the new name.");
                return;
            }
            if (conversable.isConversing()) {
                conversable.sendRawMessage(RED + "Please complete your current operation first.");
                return;
            }
            Conversation conversation = updateNameFactory.buildConversation(conversable);
            conversation.getContext().setSessionData("itemType", itemType);
            conversation.begin();
        } else {
            itemType.setName(String.join(" ", args));
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            try {
                itemService.save(itemType);
                sender.sendMessage(GREEN + "Name set.");
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                sender.sendMessage(RED + "An error occured while setting the name.");
            }
        }
    }

    private void updateItemTypeCategories(CommandSender sender, NuminousItemType itemType, String[] args) {
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        int page = -1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        if (args.length < 1 || page != -1) {
            PaginatedView.fromChatComponents(
                    new BaseComponent[] { new TextComponent("Categories") },
                    Arrays.stream(NuminousItemCategory.values()).map(category -> {
                        TextComponent button;
                        if (itemType.getCategories().contains(category)) {
                            button = new TextComponent("-");
                            button.setColor(RED);
                            button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " categories remove " + category.name()));
                            button.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to remove this category.")));
                        } else {
                            button = new TextComponent("+");
                            button.setColor(GREEN);
                            button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " categories add " + category.name()));
                            button.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add this category.")));
                        }
                        TextComponent categoryName = new TextComponent(" " + category);
                        categoryName.setColor(ChatColor.WHITE);
                        return new BaseComponent[] { button, categoryName };
                    }).toList(),
                    GREEN + "< Previous",
                    "Click here to view the previous page",
                    GREEN + "Next >",
                    "Click here to view the next page",
                    (pageNumber) -> "Page " + pageNumber,
                    10,
                    (pageNumber) -> "/numinousmodify " + itemType.getId() + "categories " + pageNumber
            );
            return;
        }
        String action = args[0];
        if (action.equalsIgnoreCase("add")) {
            if (args.length < 2) {
                displayCategoryAddMenu(sender, itemType, 1);
                return;
            }
            NuminousItemCategory category;
            try {
                category = NuminousItemCategory.valueOf(args[1]);
            } catch (IllegalArgumentException categoryException) {
                try {
                    page = Integer.parseInt(args[1]);
                    displayCategoryAddMenu(sender, itemType, page);
                    return;
                } catch (IllegalArgumentException numberException) {
                    sender.sendMessage(RED + "Invalid category.");
                    return;
                }
            }
            itemType.getCategories().add(category);
            try {
                itemService.save(itemType);
                sender.sendMessage(GREEN + "Category added.");
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                sender.sendMessage(RED + "An error occured while adding the category.");
            }
            if (sender instanceof Player player) {
                player.performCommand("numinousmodify " + itemType.getId());
            }
        } else if (action.equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sender.sendMessage(RED + "Usage: /numinousmodify " + itemType.getId() + " categories remove [category]");
                return;
            }
            NuminousItemCategory category = NuminousItemCategory.valueOf(args[1]);
            itemType.getCategories().remove(category);
            try {
                itemService.save(itemType);
                sender.sendMessage(GREEN + "Category removed.");
                if (sender instanceof Player player) {
                    player.performCommand("numinousmodify " + itemType.getId());
                }
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                sender.sendMessage(RED + "An error occured while removing the category.");
            }
        }
    }

    private void displayCategoryAddMenu(CommandSender sender, NuminousItemType itemType, int currentPage) {
        PaginatedView view = PaginatedView.fromChatComponents(
                new BaseComponent[] { new TextComponent("Categories") },
                Arrays.stream(NuminousItemCategory.values()).map(category -> {
                    TextComponent button = new TextComponent(category.toString());
                    button.setColor(GREEN);
                    button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " categories add " + category.name()));
                    button.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add the category " + category)));
                    return new BaseComponent[] { button };
                }).toList(),
                GREEN + "< Previous",
                "Click here to view the previous page",
                GREEN + "Next >",
                "Click here to view the next page",
                (pageNumber) -> "Page " + pageNumber,
                10,
                (pageNumber) -> "/numinousmodify " + itemType.getId() + " categories add " + pageNumber
        );
        if (view.isPageValid(currentPage)) {
            view.sendPage(sender, currentPage);
        } else {
            sender.sendMessage(RED + "Invalid page number.");
        }
    }

    private void updateItemTypeRarity(CommandSender sender, NuminousItemType itemType, String[] args) {
        int page = -1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        } else {
            page = 1;
        }
        if (page != -1) {
            displaySetRarityMenu(sender, itemType, page);
            return;
        }
        NuminousRarity rarity;
        try {
            rarity = NuminousRarity.valueOf(args[0]);
        } catch (IllegalArgumentException rarityException) {
            sender.sendMessage(RED + "Invalid rarity.");
            return;
        }
        itemType.setRarity(rarity);
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        try {
            itemService.save(itemType);
            sender.sendMessage(GREEN + "Rarity set.");
            if (sender instanceof Player player) {
                player.performCommand("numinousmodify " + itemType.getId());
            }
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
            sender.sendMessage(RED + "An error occured while setting the rarity.");
        }
    }

    private void displaySetRarityMenu(CommandSender sender, NuminousItemType itemType, int page) {
        PaginatedView view = PaginatedView.fromChatComponents(
                new BaseComponent[] { new TextComponent("Rarity") },
                Arrays.stream(NuminousRarity.values()).map(rarity -> {
                    TextComponent button = new TextComponent(rarity.getDisplayName());
                    button.setColor(rarity.getColor());
                    button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + itemType.getId() + " rarity " + rarity.name()));
                    button.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to set the rarity to " + rarity.getDisplayName())));
                    return new BaseComponent[] { button };
                }).toList(),
                GREEN + "< Previous",
                "Click here to view the previous page",
                GREEN + "Next >",
                "Click here to view the next page",
                (pageNumber) -> "Page " + pageNumber,
                10,
                (pageNumber) -> "/numinousmodify " + itemType.getId() + " rarity " + pageNumber
        );
        if (view.isPageValid(page)) {
            view.sendPage(sender, page);
        } else {
            sender.sendMessage(RED + "Invalid page number.");
        }
    }

    private void updateItemTypeMinecraftItem(CommandSender sender, NuminousItemType itemType) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to use this command.");
            return;
        }

        itemType.setMinecraftItem(player.getInventory().getItemInMainHand());
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        try {
            itemService.save(itemType);
            sender.sendMessage(GREEN + "Minecraft item set.");
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
            sender.sendMessage(RED + "An error occured while setting the Minecraft item.");
        }
        player.performCommand("numinousmodify " + itemType.getId());
    }

    private ConversationFactory updateWeightFactory;

    private class WeightPrompt extends NumericPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return "Enter the new weight for the item type.";
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, Number input) {
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
            try {
                itemType.setWeight(new Weight(input.doubleValue(), WeightUnit.LB));
                itemService.save(itemType);
                return new WeightSetPrompt();
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                return new WeightSetFailedPrompt();
            }
        }
    }

    private class WeightSetPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return GREEN + "Weight set.";
        }
    }

    private class WeightSetFailedPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return RED + "An error occured while setting the weight.";
        }
    }

    private void updateItemTypeWeight(CommandSender sender, NuminousItemType itemType, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Conversable conversable)) {
                sender.sendMessage(RED + "You must specify the weight.");
                return;
            }
            if (conversable.isConversing()) {
                conversable.sendRawMessage(RED + "Please complete your current operation first.");
                return;
            }
            Conversation conversation = updateWeightFactory.buildConversation(conversable);
            conversation.getContext().setSessionData("itemType", itemType);
            conversation.begin();
            return;
        }
        double weight;
        try {
            weight = Double.parseDouble(args[0]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(RED + "Invalid weight.");
            return;
        }
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        try {
            itemType.setWeight(new Weight(weight, WeightUnit.LB));
            itemService.save(itemType);
            sender.sendMessage(GREEN + "Weight set.");
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
            sender.sendMessage(RED + "An error occured while setting the weight.");
        }
    }

    private ConversationFactory updateInventorySlotsFactory;

    private class InventorySlotsPrompt extends NumericPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return "Enter the new number of inventory slots for the item type.";
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, Number input) {
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
            try {
                itemType.setInventorySlots(input.intValue());
                itemService.save(itemType);
                return new InventorySlotsSetPrompt();
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                return new InventorySlotsSetFailedPrompt();
            }
        }
    }

    private class InventorySlotsSetPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return GREEN + "Inventory slots set.";
        }
    }

    private class InventorySlotsSetFailedPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return RED + "An error occured while setting the inventory slots.";
        }
    }

    private void updateItemTypeInventorySlots(CommandSender sender, NuminousItemType itemType, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Conversable conversable)) {
                sender.sendMessage(RED + "You must specify the number of inventory slots.");
                return;
            }
            if (conversable.isConversing()) {
                conversable.sendRawMessage(RED + "Please complete your current operation first.");
                return;
            }
            Conversation conversation = updateInventorySlotsFactory.buildConversation(conversable);
            conversation.getContext().setSessionData("itemType", itemType);
            conversation.begin();
            return;
        }
        int inventorySlots;
        try {
            inventorySlots = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(RED + "Invalid number of inventory slots.");
            return;
        }
        itemType.setInventorySlots(inventorySlots);
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        try {
            itemService.save(itemType);
            sender.sendMessage(GREEN + "Inventory slots set.");
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
            sender.sendMessage(RED + "An error occured while setting the inventory slots.");
        }
    }

    private void updateItemTypeAllowLogEntries(CommandSender sender, NuminousItemType itemType, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(RED + "Usage: /numinousmodify " + itemType.getId() + " allowlogentries [true|false]");
            return;
        }
        boolean allowLogEntries = Boolean.parseBoolean(args[0]);
        itemType.setAllowLogEntries(allowLogEntries);
        NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
        try {
            itemService.save(itemType);
            sender.sendMessage(GREEN + "Log entries " + (allowLogEntries ? "enabled" : "disabled") + ".");
        } catch (IOException exception) {
            plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
            sender.sendMessage(RED + "An error occured while setting whether log entries are allowed.");
        }
        if (sender instanceof Player player) {
            player.performCommand("numinousmodify " + itemType.getId());
        }
    }

    private ConversationFactory updateLoreFactory;

    private class LorePrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return "Enter the new line of lore.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
            ItemMeta meta = itemType.getMinecraftItem().getItemMeta();
            List<String> lore = null;
            if (meta != null) {
                lore = meta.getLore();
            }
            if (lore == null) {
                lore = new ArrayList<>();
            }
            if (context.getSessionData("line") != null) {
                int line = (int) context.getSessionData("line");
                lore.add(line, ChatColor.translateAlternateColorCodes('&', input));
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&', input));
            }
            meta.setLore(lore);
            itemType.getMinecraftItem().setItemMeta(meta);
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            try {
                itemService.save(itemType);
                return new LoreSetPrompt();
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                return new LoreSetFailedPrompt();
            }
        }
    }

    private class LoreSetPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return GREEN + "Lore line added.";
        }
    }

    private class LoreSetFailedPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if (context.getForWhom() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    NuminousItemType itemType = (NuminousItemType) context.getSessionData("itemType");
                    player.performCommand("numinousmodify " + itemType.getId());
                });
            }
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return RED + "An error occured while adding a line of lore.";
        }
    }

    private void updateItemTypeLore(CommandSender sender, NuminousItemType itemType, String[] args) {
        String action;
        if (args.length == 0) {
            action = "";
        } else {
            action = args[0];
        }
        if (action.equalsIgnoreCase("add")) {
            if (args.length < 2) {
                sender.sendMessage(RED + "Usage: /numinousmodify " + itemType.getId() + " lore add [line number]");
                return;
            }
            int lineNum;
            try {
                lineNum = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(RED + "Invalid line number.");
                return;
            }
            if (args.length == 1) {
                if (!(sender instanceof Conversable conversable)) {
                    sender.sendMessage(RED + "You must specify the line of lore to add.");
                    return;
                }
                if (conversable.isConversing()) {
                    conversable.sendRawMessage(RED + "Please complete your current operation first.");
                    return;
                }
                updateLoreFactory.buildConversation(conversable).begin();
                return;
            }
            if (args.length < 3) {
                Conversation conversation = updateLoreFactory.buildConversation((Conversable) sender);
                conversation.getContext().setSessionData("itemType", itemType);
                conversation.getContext().setSessionData("line", lineNum);
                conversation.begin();
            } else {
                String line = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                ItemMeta meta = itemType.getMinecraftItem().getItemMeta();
                List<String> lore = null;
                if (meta != null) {
                    lore = meta.getLore();
                }
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add(lineNum, ChatColor.translateAlternateColorCodes('&', line));
                meta.setLore(lore);
                itemType.getMinecraftItem().setItemMeta(meta);
                NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
                try {
                    itemService.save(itemType);
                    sender.sendMessage(GREEN + "Lore line added.");
                } catch (IOException exception) {
                    plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                    sender.sendMessage(RED + "An error occured while adding a line of lore.");
                }
                if (sender instanceof Player player) {
                    player.performCommand("numinousmodify " + itemType.getId());
                }
            }
        } else if (action.equalsIgnoreCase("remove")) {
            int lineNum;
            try {
                lineNum = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(RED + "Invalid line number.");
                return;
            }
            if (args.length == 1) {
                if (!(sender instanceof Conversable conversable)) {
                    sender.sendMessage(RED + "You must specify the line of lore to remove.");
                    return;
                }
                if (conversable.isConversing()) {
                    conversable.sendRawMessage(RED + "Please complete your current operation first.");
                    return;
                }
                updateLoreFactory.buildConversation(conversable).begin();
                return;
            }
            ItemMeta meta = itemType.getMinecraftItem().getItemMeta();
            List<String> lore = null;
            if (meta != null) {
                lore = meta.getLore();
            }
            if (lore == null) {
                lore = new ArrayList<>();
            }
            if (lineNum < 0 || lineNum >= lore.size()) {
                sender.sendMessage(RED + "Invalid line number.");
                return;
            }
            lore.remove(lineNum);
            meta.setLore(lore);
            itemType.getMinecraftItem().setItemMeta(meta);
            NuminousItemService itemService = Services.INSTANCE.get(NuminousItemService.class);
            try {
                itemService.save(itemType);
                sender.sendMessage(GREEN + "Lore line removed.");
            } catch (IOException exception) {
                plugin.getLogger().log(SEVERE, "Failed to save item type", exception);
                sender.sendMessage(RED + "An error occured while removing a line of lore.");
            }
            if (sender instanceof Player player) {
                player.performCommand("numinousmodify " + itemType.getId());
            }
        } else {
            ItemMeta meta = itemType.getMinecraftItem().getItemMeta();
            List<String> lore = null;
            if (meta != null) {
                lore = meta.getLore();
            }
            if (lore == null) {
                lore = new ArrayList<>();
            }
            sender.spigot().sendMessage(
                    new TextComponent("Lore")
            );
            final NuminousItemType finalItemType = itemType;
            final List<String> finalLore = lore;
            Stream<BaseComponent[]> loreLines = IntStream.range(0, finalLore.size()).boxed().flatMap(lineNum -> {
                String line = finalLore.get(lineNum);
                TextComponent addButton1 = null;
                if (lineNum == 0) {
                    addButton1 = new TextComponent("+");
                    addButton1.setColor(GREEN);
                    addButton1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore add " + lineNum));
                    addButton1.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add a line of lore here.")));
                }
                TextComponent removeButton = new TextComponent("-");
                removeButton.setColor(RED);
                removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore remove " + lineNum));
                removeButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to remove this line of lore.")));
                TextComponent lineComponent = new TextComponent(" " + line);
                lineComponent.setColor(ChatColor.WHITE);
                TextComponent addButton2 = new TextComponent("+");
                addButton2.setColor(GREEN);
                addButton2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/numinousmodify " + finalItemType.getId() + " lore add " + (lineNum + 1)));
                addButton2.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to add a line of lore here.")));
                if (addButton1 == null) {
                    return Stream.of(
                            new BaseComponent[]{removeButton, lineComponent},
                            new BaseComponent[]{addButton2}
                    );
                } else {
                    return Stream.of(
                            new BaseComponent[]{addButton1},
                            new BaseComponent[]{removeButton, lineComponent},
                            new BaseComponent[]{addButton2}
                    );
                }
            });
            loreLines.forEach(loreLine -> sender.spigot().sendMessage(loreLine));
        }
    }

}
