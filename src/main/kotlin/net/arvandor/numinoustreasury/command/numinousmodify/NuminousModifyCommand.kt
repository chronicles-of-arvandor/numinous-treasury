package net.arvandor.numinoustreasury.command.numinousmodify

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.item.NuminousItemCategory
import net.arvandor.numinoustreasury.item.NuminousItemService
import net.arvandor.numinoustreasury.item.NuminousItemType
import net.arvandor.numinoustreasury.item.NuminousRarity
import net.arvandor.numinoustreasury.measurement.Weight
import net.arvandor.numinoustreasury.measurement.WeightUnit
import net.arvandor.numinoustreasury.utils.Args
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.conversations.Conversable
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.MessagePrompt
import org.bukkit.conversations.NumericPrompt
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.io.IOException
import java.util.logging.Level

class NuminousModifyCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.numinousmodify")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to modify numinous items.")
            return true
        }
        val unquotedArgs = Args.unquote(args)

        if (unquotedArgs.isEmpty()) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /numinousmodify [item id] (action)")
            return true
        }

        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )

        val itemId = unquotedArgs[0]
        var itemType = itemService.getItemTypeById(itemId)
        if (itemType == null) {
            itemType = itemService.getItemTypeByName(itemId)
        }
        if (itemType == null) {
            sender.sendMessage(ChatColor.RED.toString() + "No item type found with id or name " + itemId)
            return true
        }

        var page = -1
        if (unquotedArgs.size >= 2) {
            try {
                page = unquotedArgs[1].toInt()
            } catch (ignored: NumberFormatException) {
            }
        }

        if (unquotedArgs.size < 2 || page != -1) {
            if (page == -1) {
                page = 1
            }
            val itemName = TextComponent(itemType.name + " ")
            itemName.color = ChatColor.WHITE
            val editNameButton = TextComponent("(Edit)")
            editNameButton.color = ChatColor.GREEN
            editNameButton.clickEvent = ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " name")
            editNameButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit the name."))
            val title =
                arrayOf<BaseComponent>(
                    itemName,
                    editNameButton,
                )
            val categoriesHeader = TextComponent("Categories")
            categoriesHeader.color = ChatColor.WHITE
            categoriesHeader.setBold(true)
            val addCategoryButton = TextComponent("+")
            addCategoryButton.color = ChatColor.GREEN
            addCategoryButton.clickEvent = ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " categories add")
            addCategoryButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add a category."))
            val finalItemType = itemType
            val categoriesLines =
                arrayOf(
                    arrayOf<BaseComponent>(categoriesHeader),
                    *itemType.categories.map { category ->
                        val removeButton = TextComponent("-")
                        removeButton.color = ChatColor.RED
                        removeButton.clickEvent =
                            ClickEvent(
                                RUN_COMMAND,
                                "/numinousmodify " + finalItemType.id + " categories remove " + category.name,
                            )
                        removeButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to remove this category."))
                        val categoryName = TextComponent(" " + category.name)
                        categoryName.color = ChatColor.WHITE
                        arrayOf<BaseComponent>(removeButton, categoryName)
                    }.toTypedArray(),
                    arrayOf<BaseComponent>(addCategoryButton),
                )

            val rarityTitle = TextComponent("Rarity: ")
            rarityTitle.color = ChatColor.WHITE
            rarityTitle.setBold(true)
            val rarity = TextComponent.fromLegacyText(itemType.rarity.color.toString() + itemType.rarity.displayName)
            val editRarityButton = TextComponent(" (Edit)")
            editRarityButton.color = ChatColor.GREEN
            editRarityButton.clickEvent = ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " rarity")
            editRarityButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit the rarity."))
            val rarityLine =
                arrayOf(
                    rarityTitle,
                    *rarity,
                    editRarityButton,
                )

            val minecraftItemTitle = TextComponent("Minecraft Item: ")
            minecraftItemTitle.color = ChatColor.WHITE
            minecraftItemTitle.setBold(true)
            val minecraftItem = TextComponent.fromLegacyText(itemType.minecraftItem.type.name)
            val editMinecraftItemButton = TextComponent(" (Update to held item)")
            editMinecraftItemButton.color = ChatColor.GREEN
            editMinecraftItemButton.clickEvent =
                ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " minecraftitem")
            editMinecraftItemButton.hoverEvent =
                HoverEvent(SHOW_TEXT, Text("Click here to update the Minecraft item to the item in your hand."))
            val minecraftItemLine =
                arrayOf(
                    minecraftItemTitle,
                    *minecraftItem,
                    editMinecraftItemButton,
                )

            val weightTitle = TextComponent("Weight: ")
            weightTitle.color = ChatColor.WHITE
            weightTitle.setBold(true)
            val weight = TextComponent.fromLegacyText(itemType.weight.toString())
            val editWeightButton = TextComponent(" (Edit)")
            editWeightButton.color = ChatColor.GREEN
            editWeightButton.clickEvent = ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " weight")
            editWeightButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit the weight."))
            val weightLine =
                arrayOf(
                    weightTitle,
                    *weight,
                    editWeightButton,
                )

            val inventorySlotsTitle = TextComponent("Inventory Slots: ")
            inventorySlotsTitle.color = ChatColor.WHITE
            inventorySlotsTitle.setBold(true)
            val inventorySlots = TextComponent.fromLegacyText(itemType.inventorySlots.toString())
            val editInventorySlotsButton = TextComponent(" (Edit)")
            editInventorySlotsButton.color = ChatColor.GREEN
            editInventorySlotsButton.clickEvent =
                ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " inventoryslots")
            editInventorySlotsButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit the inventory slots."))
            val inventorySlotsLine =
                arrayOf(
                    inventorySlotsTitle,
                    *inventorySlots,
                    editInventorySlotsButton,
                )

            val allowLogEntriesTitle = TextComponent("Allow Log Entries: ")
            allowLogEntriesTitle.color = ChatColor.WHITE
            allowLogEntriesTitle.setBold(true)
            val allowLogEntries = TextComponent.fromLegacyText(itemType.isAllowLogEntries.toString())
            val editAllowLogEntriesButton = TextComponent(" (Toggle)")
            editAllowLogEntriesButton.color = ChatColor.GREEN
            editAllowLogEntriesButton.clickEvent =
                ClickEvent(
                    RUN_COMMAND,
                    "/numinousmodify " + itemType.id + " allowlogentries " + !itemType.isAllowLogEntries,
                )
            editAllowLogEntriesButton.hoverEvent =
                HoverEvent(SHOW_TEXT, Text("Click here to edit whether log entries are allowed."))
            val allowLogEntriesLine =
                arrayOf(
                    allowLogEntriesTitle,
                    *allowLogEntries,
                    editAllowLogEntriesButton,
                )

            val meta = itemType.minecraftItem.itemMeta
            var lore: List<String>? = null
            if (meta != null) {
                lore = meta.lore
            }
            if (lore == null) {
                lore = mutableListOf()
            }
            val loreTitle = TextComponent("Lore")
            loreTitle.color = ChatColor.WHITE
            loreTitle.setBold(true)
            if (lore.isEmpty()) {
                val addButton = TextComponent("+")
                addButton.color = ChatColor.GREEN
                addButton.clickEvent = ClickEvent(RUN_COMMAND, "/numinousmodify " + finalItemType.id + " lore add 0")
                addButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add a line of lore here."))
                sender.spigot().sendMessage(addButton)
            }
            val finalLore: List<String> = lore
            val loreLines =
                finalLore.indices.flatMap { lineNum: Int ->
                    val line = finalLore[lineNum]
                    var addButton1: TextComponent? = null
                    if (lineNum == 0) {
                        addButton1 = TextComponent("+")
                        addButton1.color = ChatColor.GREEN
                        addButton1.clickEvent =
                            ClickEvent(RUN_COMMAND, "/numinousmodify " + finalItemType.id + " lore add " + lineNum)
                        addButton1.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add a line of lore here."))
                    }
                    val removeButton = TextComponent("-")
                    removeButton.color = ChatColor.RED
                    removeButton.clickEvent =
                        ClickEvent(RUN_COMMAND, "/numinousmodify " + finalItemType.id + " lore remove " + lineNum)
                    removeButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to remove this line of lore."))
                    val lineComponent = TextComponent(" $line")
                    lineComponent.color = ChatColor.WHITE
                    val addButton2 = TextComponent("+")
                    addButton2.color = ChatColor.GREEN
                    addButton2.clickEvent =
                        ClickEvent(RUN_COMMAND, "/numinousmodify " + finalItemType.id + " lore add " + (lineNum + 1))
                    addButton2.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add a line of lore here."))
                    if (addButton1 == null) {
                        return@flatMap listOf(
                            arrayOf<BaseComponent>(removeButton, lineComponent),
                            arrayOf<BaseComponent>(addButton2),
                        )
                    } else {
                        return@flatMap listOf(
                            arrayOf<BaseComponent>(addButton1),
                            arrayOf<BaseComponent>(removeButton, lineComponent),
                            arrayOf<BaseComponent>(addButton2),
                        )
                    }
                }.toTypedArray()
            val view =
                PaginatedView.fromChatComponents(
                    title,
                    listOf(
                        *categoriesLines,
                        rarityLine,
                        minecraftItemLine,
                        weightLine,
                        inventorySlotsLine,
                        allowLogEntriesLine,
                        arrayOf<BaseComponent>(loreTitle),
                        *loreLines,
                    ),
                    ChatColor.GREEN.toString() + "< Previous",
                    "Click here to view the previous page",
                    ChatColor.GREEN.toString() + "Next >",
                    "Click here to view the next page",
                    { pageNumber: Int -> "Page $pageNumber" },
                    10,
                    { pageNumber: Int -> "/numinousmodify " + finalItemType.id + " " + pageNumber },
                )

            if (view.isPageValid(page)) {
                view.sendPage(sender, page)
            } else {
                sender.sendMessage(ChatColor.RED.toString() + "Invalid page number.")
            }
        } else {
            val action = unquotedArgs[1]
            if (action.equals("name", ignoreCase = true)) {
                updateItemTypeName(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            } else if (action.equals("categories", ignoreCase = true)) {
                updateItemTypeCategories(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            } else if (action.equals("rarity", ignoreCase = true)) {
                updateItemTypeRarity(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            } else if (action.equals("minecraftitem", ignoreCase = true)) {
                updateItemTypeMinecraftItem(sender, itemType)
            } else if (action.equals("weight", ignoreCase = true)) {
                updateItemTypeWeight(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            } else if (action.equals("inventoryslots", ignoreCase = true)) {
                updateItemTypeInventorySlots(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            } else if (action.equals("allowlogentries", ignoreCase = true)) {
                updateItemTypeAllowLogEntries(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            } else if (action.equals("lore", ignoreCase = true)) {
                updateItemTypeLore(
                    sender,
                    itemType,
                    unquotedArgs.drop(2).toTypedArray(),
                )
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        when (args.size) {
            1 -> {
                val itemService =
                    Services.INSTANCE.get(
                        NuminousItemService::class.java,
                    )
                return itemService.itemTypes
                    .map { obj -> obj.id }
                    .filter { id: String ->
                        id.lowercase().startsWith(args[0].lowercase())
                    }
            }
            2 -> {
                return listOf(
                    "name",
                    "categories",
                    "rarity",
                    "minecraftitem",
                    "weight",
                    "inventoryslots",
                    "allowlogentries",
                    "lore",
                )
            }
            3 -> {
                when (args[1].lowercase()) {
                    "categories" ->
                        return NuminousItemCategory.entries
                            .map { obj -> obj.name }
                            .filter { category: String ->
                                category.lowercase().startsWith(args[2].lowercase())
                            }

                    "rarity" ->
                        return NuminousRarity.entries
                            .map { obj: NuminousRarity -> obj.name }
                            .filter { rarity: String ->
                                rarity.lowercase().startsWith(args[2].lowercase())
                            }

                    "lore" -> return listOf("add", "remove").filter { action: String ->
                        action.lowercase().startsWith(
                            args[2].lowercase(),
                        )
                    }

                    "allowlogentries" -> return listOf("true", "false").filter { value: String ->
                        value.lowercase().startsWith(
                            args[2].lowercase(),
                        )
                    }
                }
            }
        }
        return listOf()
    }

    private val updateNameFactory: ConversationFactory

    private inner class NamePrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return "Enter the new name for the item type."
        }

        override fun acceptInput(
            context: ConversationContext,
            input: String?,
        ): Prompt? {
            if (input == null) {
                return END_OF_CONVERSATION
            }
            val itemService =
                Services.INSTANCE.get(
                    NuminousItemService::class.java,
                )
            val itemType = context.getSessionData("itemType") as NuminousItemType
            itemType.name = input
            try {
                itemService.save(itemType)
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                return NameSetFailedPrompt()
            }
            return NameSetPrompt()
        }
    }

    private inner class NameSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType
                        player.performCommand("numinousmodify " + itemType.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.GREEN.toString() + "Name set."
        }
    }

    private inner class NameSetFailedPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType
                        player.performCommand("numinousmodify " + itemType.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.RED.toString() + "An error occured while setting the name."
        }
    }

    private fun updateItemTypeName(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        if (args.size == 0) {
            if (sender !is Conversable) {
                sender.sendMessage(ChatColor.RED.toString() + "You must specify the new name.")
                return
            }
            if (sender.isConversing) {
                sender.sendRawMessage(ChatColor.RED.toString() + "Please complete your current operation first.")
                return
            }
            val conversation = updateNameFactory.buildConversation(sender)
            conversation.context.setSessionData("itemType", itemType)
            conversation.begin()
        } else {
            itemType.name = args.joinToString(" ")
            val itemService =
                Services.INSTANCE.get(
                    NuminousItemService::class.java,
                )
            try {
                itemService.save(itemType)
                sender.sendMessage(ChatColor.GREEN.toString() + "Name set.")
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                sender.sendMessage(ChatColor.RED.toString() + "An error occured while setting the name.")
            }
        }
    }

    private fun updateItemTypeCategories(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        var page = -1
        if (args.size >= 1) {
            try {
                page = args[0].toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        if (args.size < 1 || page != -1) {
            PaginatedView.fromChatComponents(
                arrayOf<BaseComponent>(TextComponent("Categories")),
                NuminousItemCategory.entries.map { category ->
                    val button: TextComponent
                    if (itemType.categories.contains(category)) {
                        button = TextComponent("-")
                        button.color = ChatColor.RED
                        button.clickEvent =
                            ClickEvent(
                                RUN_COMMAND,
                                "/numinousmodify " + itemType.id + " categories remove " + category.name,
                            )
                        button.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to remove this category."))
                    } else {
                        button = TextComponent("+")
                        button.color = ChatColor.GREEN
                        button.clickEvent =
                            ClickEvent(
                                RUN_COMMAND,
                                "/numinousmodify " + itemType.id + " categories add " + category.name,
                            )
                        button.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add this category."))
                    }
                    val categoryName = TextComponent(" $category")
                    categoryName.color = ChatColor.WHITE
                    arrayOf<BaseComponent>(button, categoryName)
                },
                ChatColor.GREEN.toString() + "< Previous",
                "Click here to view the previous page",
                ChatColor.GREEN.toString() + "Next >",
                "Click here to view the next page",
                { pageNumber: Int -> "Page $pageNumber" },
                10,
                { pageNumber: Int -> "/numinousmodify " + itemType.id + "categories " + pageNumber },
            )
            return
        }
        val action = args[0]
        if (action.equals("add", ignoreCase = true)) {
            if (args.size < 2) {
                displayCategoryAddMenu(sender, itemType, 1)
                return
            }
            val category: NuminousItemCategory
            try {
                category = NuminousItemCategory.valueOf(args[1])
            } catch (categoryException: IllegalArgumentException) {
                try {
                    page = args[1].toInt()
                    displayCategoryAddMenu(sender, itemType, page)
                    return
                } catch (numberException: IllegalArgumentException) {
                    sender.sendMessage(ChatColor.RED.toString() + "Invalid category.")
                    return
                }
            }
            itemType.categories.add(category)
            try {
                itemService.save(itemType)
                sender.sendMessage(ChatColor.GREEN.toString() + "Category added.")
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                sender.sendMessage(ChatColor.RED.toString() + "An error occured while adding the category.")
            }
            if (sender is Player) {
                sender.performCommand("numinousmodify " + itemType.id)
            }
        } else if (action.equals("remove", ignoreCase = true)) {
            if (args.size < 2) {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /numinousmodify " + itemType.id + " categories remove [category]")
                return
            }
            val category = NuminousItemCategory.valueOf(args[1])
            itemType.categories.remove(category)
            try {
                itemService.save(itemType)
                sender.sendMessage(ChatColor.GREEN.toString() + "Category removed.")
                if (sender is Player) {
                    sender.performCommand("numinousmodify " + itemType.id)
                }
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                sender.sendMessage(ChatColor.RED.toString() + "An error occured while removing the category.")
            }
        }
    }

    private fun displayCategoryAddMenu(
        sender: CommandSender,
        itemType: NuminousItemType,
        currentPage: Int,
    ) {
        val view =
            PaginatedView.fromChatComponents(
                arrayOf<BaseComponent>(TextComponent("Categories")),
                NuminousItemCategory.entries.map { category ->
                    val button = TextComponent(category.toString())
                    button.color = ChatColor.GREEN
                    button.clickEvent =
                        ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " categories add " + category.name)
                    button.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add the category $category"))
                    arrayOf<BaseComponent>(button)
                },
                ChatColor.GREEN.toString() + "< Previous",
                "Click here to view the previous page",
                ChatColor.GREEN.toString() + "Next >",
                "Click here to view the next page",
                { pageNumber: Int -> "Page $pageNumber" },
                10,
                { pageNumber: Int -> "/numinousmodify " + itemType.id + " categories add " + pageNumber },
            )
        if (view.isPageValid(currentPage)) {
            view.sendPage(sender, currentPage)
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid page number.")
        }
    }

    private fun updateItemTypeRarity(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        var page = -1
        if (args.size >= 1) {
            try {
                page = args[0].toInt()
            } catch (ignored: NumberFormatException) {
            }
        } else {
            page = 1
        }
        if (page != -1) {
            displaySetRarityMenu(sender, itemType, page)
            return
        }
        val rarity: NuminousRarity
        try {
            rarity = NuminousRarity.valueOf(args[0])
        } catch (rarityException: IllegalArgumentException) {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid rarity.")
            return
        }
        itemType.rarity = rarity
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        try {
            itemService.save(itemType)
            sender.sendMessage(ChatColor.GREEN.toString() + "Rarity set.")
            if (sender is Player) {
                sender.performCommand("numinousmodify " + itemType.id)
            }
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
            sender.sendMessage(ChatColor.RED.toString() + "An error occured while setting the rarity.")
        }
    }

    private fun displaySetRarityMenu(
        sender: CommandSender,
        itemType: NuminousItemType,
        page: Int,
    ) {
        val view =
            PaginatedView.fromChatComponents(
                arrayOf<BaseComponent>(TextComponent("Rarity")),
                NuminousRarity.entries.map { rarity ->
                    val button = TextComponent(rarity.displayName)
                    button.color = rarity.color
                    button.clickEvent = ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " rarity " + rarity.name)
                    button.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to set the rarity to " + rarity.displayName))
                    arrayOf<BaseComponent>(button)
                },
                ChatColor.GREEN.toString() + "< Previous",
                "Click here to view the previous page",
                ChatColor.GREEN.toString() + "Next >",
                "Click here to view the next page",
                { pageNumber: Int -> "Page $pageNumber" },
                10,
                { pageNumber: Int -> "/numinousmodify " + itemType.id + " rarity " + pageNumber },
            )
        if (view.isPageValid(page)) {
            view.sendPage(sender, page)
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid page number.")
        }
    }

    private fun updateItemTypeMinecraftItem(
        sender: CommandSender,
        itemType: NuminousItemType,
    ) {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to use this command.")
            return
        }

        itemType.minecraftItem = sender.inventory.itemInMainHand
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        try {
            itemService.save(itemType)
            sender.sendMessage(ChatColor.GREEN.toString() + "Minecraft item set.")
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
            sender.sendMessage(ChatColor.RED.toString() + "An error occured while setting the Minecraft item.")
        }
        sender.performCommand("numinousmodify " + itemType.id)
    }

    private val updateWeightFactory: ConversationFactory

    private inner class WeightPrompt : NumericPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return "Enter the new weight for the item type."
        }

        public override fun acceptValidatedInput(
            context: ConversationContext,
            input: Number,
        ): Prompt {
            val itemService =
                Services.INSTANCE.get(
                    NuminousItemService::class.java,
                )
            val itemType = context.getSessionData("itemType") as NuminousItemType?
            try {
                itemType!!.weight = Weight(input.toDouble(), WeightUnit.Companion.LB)
                itemService.save(itemType)
                return WeightSetPrompt()
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                return WeightSetFailedPrompt()
            }
        }
    }

    private inner class WeightSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType?
                        player.performCommand("numinousmodify " + itemType!!.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.GREEN.toString() + "Weight set."
        }
    }

    private inner class WeightSetFailedPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType
                        player.performCommand("numinousmodify " + itemType.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.RED.toString() + "An error occured while setting the weight."
        }
    }

    private fun updateItemTypeWeight(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        if (args.size == 0) {
            if (sender !is Conversable) {
                sender.sendMessage(ChatColor.RED.toString() + "You must specify the weight.")
                return
            }
            if (sender.isConversing) {
                sender.sendRawMessage(ChatColor.RED.toString() + "Please complete your current operation first.")
                return
            }
            val conversation = updateWeightFactory.buildConversation(sender)
            conversation.context.setSessionData("itemType", itemType)
            conversation.begin()
            return
        }
        val weight: Double
        try {
            weight = args[0].toDouble()
        } catch (exception: NumberFormatException) {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid weight.")
            return
        }
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        try {
            itemType.weight = Weight(weight, WeightUnit.Companion.LB)
            itemService.save(itemType)
            sender.sendMessage(ChatColor.GREEN.toString() + "Weight set.")
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
            sender.sendMessage(ChatColor.RED.toString() + "An error occured while setting the weight.")
        }
    }

    private val updateInventorySlotsFactory: ConversationFactory

    private inner class InventorySlotsPrompt : NumericPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return "Enter the new number of inventory slots for the item type."
        }

        public override fun acceptValidatedInput(
            context: ConversationContext,
            input: Number,
        ): Prompt {
            val itemService =
                Services.INSTANCE.get(
                    NuminousItemService::class.java,
                )
            val itemType = context.getSessionData("itemType") as NuminousItemType
            try {
                itemType.inventorySlots = input.toInt()
                itemService.save(itemType)
                return InventorySlotsSetPrompt()
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                return InventorySlotsSetFailedPrompt()
            }
        }
    }

    private inner class InventorySlotsSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType?
                        player.performCommand("numinousmodify " + itemType!!.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.GREEN.toString() + "Inventory slots set."
        }
    }

    private inner class InventorySlotsSetFailedPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType?
                        player.performCommand("numinousmodify " + itemType!!.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.RED.toString() + "An error occured while setting the inventory slots."
        }
    }

    private fun updateItemTypeInventorySlots(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        if (args.size == 0) {
            if (sender !is Conversable) {
                sender.sendMessage(ChatColor.RED.toString() + "You must specify the number of inventory slots.")
                return
            }
            if (sender.isConversing) {
                sender.sendRawMessage(ChatColor.RED.toString() + "Please complete your current operation first.")
                return
            }
            val conversation = updateInventorySlotsFactory.buildConversation(sender)
            conversation.context.setSessionData("itemType", itemType)
            conversation.begin()
            return
        }
        val inventorySlots: Int
        try {
            inventorySlots = args[0].toInt()
        } catch (exception: NumberFormatException) {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid number of inventory slots.")
            return
        }
        itemType.inventorySlots = inventorySlots
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        try {
            itemService.save(itemType)
            sender.sendMessage(ChatColor.GREEN.toString() + "Inventory slots set.")
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
            sender.sendMessage(ChatColor.RED.toString() + "An error occured while setting the inventory slots.")
        }
    }

    private fun updateItemTypeAllowLogEntries(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        if (args.isEmpty()) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /numinousmodify " + itemType.id + " allowlogentries [true|false]")
            return
        }
        val allowLogEntries = args[0].toBoolean()
        itemType.isAllowLogEntries = allowLogEntries
        val itemService =
            Services.INSTANCE.get(
                NuminousItemService::class.java,
            )
        try {
            itemService.save(itemType)
            sender.sendMessage(ChatColor.GREEN.toString() + "Log entries " + (if (allowLogEntries) "enabled" else "disabled") + ".")
        } catch (exception: IOException) {
            plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
            sender.sendMessage(ChatColor.RED.toString() + "An error occured while setting whether log entries are allowed.")
        }
        if (sender is Player) {
            sender.performCommand("numinousmodify " + itemType.id)
        }
    }

    private val updateLoreFactory: ConversationFactory

    init {
        this.updateNameFactory =
            ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(NamePrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener { event: ConversationAbandonedEvent ->
                    if (!event.gracefulExit()) {
                        event.context.forWhom.sendRawMessage(ChatColor.RED.toString() + "Operation cancelled.")
                    }
                }

        this.updateWeightFactory =
            ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(WeightPrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener { event: ConversationAbandonedEvent ->
                    if (!event.gracefulExit()) {
                        event.context.forWhom.sendRawMessage(ChatColor.RED.toString() + "Operation cancelled.")
                    }
                }

        this.updateInventorySlotsFactory =
            ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(InventorySlotsPrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener { event: ConversationAbandonedEvent ->
                    if (!event.gracefulExit()) {
                        event.context.forWhom.sendRawMessage(ChatColor.RED.toString() + "Operation cancelled.")
                    }
                }

        this.updateLoreFactory =
            ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(LorePrompt())
                .withEscapeSequence("cancel")
                .withLocalEcho(false)
                .addConversationAbandonedListener { event: ConversationAbandonedEvent ->
                    if (!event.gracefulExit()) {
                        event.context.forWhom.sendRawMessage(ChatColor.RED.toString() + "Operation cancelled.")
                    }
                }
    }

    private inner class LorePrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return "Enter the new line of lore."
        }

        override fun acceptInput(
            context: ConversationContext,
            input: String?,
        ): Prompt? {
            val itemType = context.getSessionData("itemType") as NuminousItemType?
            val meta = itemType!!.minecraftItem.itemMeta
            var lore: MutableList<String?>? = null
            if (meta != null) {
                lore = meta.lore
            }
            if (lore == null) {
                lore = ArrayList()
            }
            if (context.getSessionData("line") != null) {
                val line = context.getSessionData("line") as Int
                lore.add(line, ChatColor.translateAlternateColorCodes('&', input))
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&', input))
            }
            meta!!.lore = lore
            itemType.minecraftItem.setItemMeta(meta)
            val itemService =
                Services.INSTANCE.get(
                    NuminousItemService::class.java,
                )
            try {
                itemService.save(itemType)
                return LoreSetPrompt()
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                return LoreSetFailedPrompt()
            }
        }
    }

    private inner class LoreSetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType?
                        player.performCommand("numinousmodify " + itemType!!.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.GREEN.toString() + "Lore line added."
        }
    }

    private inner class LoreSetFailedPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val player = context.forWhom
            if (player is Player) {
                plugin.server.scheduler.runTask(
                    plugin,
                    Runnable {
                        val itemType = context.getSessionData("itemType") as NuminousItemType?
                        player.performCommand("numinousmodify " + itemType!!.id)
                    },
                )
            }
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.RED.toString() + "An error occured while adding a line of lore."
        }
    }

    private fun updateItemTypeLore(
        sender: CommandSender,
        itemType: NuminousItemType,
        args: Array<String>,
    ) {
        val action =
            if (args.isEmpty()) {
                ""
            } else {
                args[0]
            }
        if (action.equals("add", ignoreCase = true)) {
            val lineNum: Int
            if (args.size >= 2) {
                try {
                    lineNum = args[1].toInt()
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(ChatColor.RED.toString() + "Invalid line number.")
                    return
                }
            } else {
                if (sender !is Conversable) {
                    sender.sendMessage(ChatColor.RED.toString() + "You must specify the line of lore to add.")
                    return
                }
                if (sender.isConversing) {
                    sender.sendRawMessage(ChatColor.RED.toString() + "Please complete your current operation first.")
                    return
                }
                updateLoreFactory.buildConversation(sender).begin()
                return
            }
            if (args.size < 3) {
                val conversation = updateLoreFactory.buildConversation((sender as Conversable))
                conversation.context.setSessionData("itemType", itemType)
                conversation.context.setSessionData("line", lineNum)
                conversation.begin()
            } else {
                val line = args.copyOfRange(2, args.size).joinToString(" ")
                val meta = itemType.minecraftItem.itemMeta
                var lore: MutableList<String?>? = null
                if (meta != null) {
                    lore = meta.lore
                }
                if (lore == null) {
                    lore = mutableListOf()
                }
                lore.add(lineNum, ChatColor.translateAlternateColorCodes('&', line))
                meta!!.lore = lore
                itemType.minecraftItem.setItemMeta(meta)
                val itemService =
                    Services.INSTANCE.get(
                        NuminousItemService::class.java,
                    )
                try {
                    itemService.save(itemType)
                    sender.sendMessage(ChatColor.GREEN.toString() + "Lore line added.")
                } catch (exception: IOException) {
                    plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                    sender.sendMessage(ChatColor.RED.toString() + "An error occured while adding a line of lore.")
                }
                if (sender is Player) {
                    sender.performCommand("numinousmodify " + itemType.id)
                }
            }
        } else if (action.equals("remove", ignoreCase = true)) {
            val lineNum: Int
            if (args.size >= 2) {
                try {
                    lineNum = args[1].toInt()
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(ChatColor.RED.toString() + "Invalid line number.")
                    return
                }
            } else {
                if (sender !is Conversable) {
                    sender.sendMessage(ChatColor.RED.toString() + "You must specify the line of lore to remove.")
                    return
                }
                if (sender.isConversing) {
                    sender.sendRawMessage(ChatColor.RED.toString() + "Please complete your current operation first.")
                    return
                }
                updateLoreFactory.buildConversation(sender).begin()
                return
            }
            val meta = itemType.minecraftItem.itemMeta
            var lore: MutableList<String?>? = null
            if (meta != null) {
                lore = meta.lore
            }
            if (lore == null) {
                lore = mutableListOf()
            }
            if (lineNum < 0 || lineNum >= lore.size) {
                sender.sendMessage(ChatColor.RED.toString() + "Invalid line number.")
                return
            }
            lore.removeAt(lineNum)
            meta!!.lore = lore
            itemType.minecraftItem.setItemMeta(meta)
            val itemService =
                Services.INSTANCE.get(
                    NuminousItemService::class.java,
                )
            try {
                itemService.save(itemType)
                sender.sendMessage(ChatColor.GREEN.toString() + "Lore line removed.")
            } catch (exception: IOException) {
                plugin.logger.log(Level.SEVERE, "Failed to save item type", exception)
                sender.sendMessage(ChatColor.RED.toString() + "An error occured while removing a line of lore.")
            }
            if (sender is Player) {
                sender.performCommand("numinousmodify " + itemType.id)
            }
        } else {
            val meta = itemType.minecraftItem.itemMeta
            var lore: List<String>? = null
            if (meta != null) {
                lore = meta.lore
            }
            if (lore == null) {
                lore = mutableListOf()
            }
            sender.spigot().sendMessage(
                TextComponent("Lore"),
            )
            val finalLore: List<String> = lore
            val loreLines =
                finalLore.indices.flatMap { lineNum: Int ->
                    val line = finalLore[lineNum]
                    var addButton1: TextComponent? = null
                    if (lineNum == 0) {
                        addButton1 = TextComponent("+")
                        addButton1.color = ChatColor.GREEN
                        addButton1.clickEvent =
                            ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " lore add " + lineNum)
                        addButton1.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add a line of lore here."))
                    }
                    val removeButton = TextComponent("-")
                    removeButton.color = ChatColor.RED
                    removeButton.clickEvent =
                        ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " lore remove " + lineNum)
                    removeButton.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to remove this line of lore."))
                    val lineComponent = TextComponent(" $line")
                    lineComponent.color = ChatColor.WHITE
                    val addButton2 = TextComponent("+")
                    addButton2.color = ChatColor.GREEN
                    addButton2.clickEvent =
                        ClickEvent(RUN_COMMAND, "/numinousmodify " + itemType.id + " lore add " + (lineNum + 1))
                    addButton2.hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add a line of lore here."))
                    if (addButton1 == null) {
                        return@flatMap listOf<Array<BaseComponent>>(
                            arrayOf<BaseComponent>(removeButton, lineComponent),
                            arrayOf<BaseComponent>(addButton2),
                        )
                    } else {
                        return@flatMap listOf<Array<BaseComponent>>(
                            arrayOf<BaseComponent>(addButton1),
                            arrayOf<BaseComponent>(removeButton, lineComponent),
                            arrayOf<BaseComponent>(addButton2),
                        )
                    }
                }
            loreLines.forEach { loreLine: Array<BaseComponent> -> sender.spigot().sendMessage(*loreLine) }
        }
    }
}
