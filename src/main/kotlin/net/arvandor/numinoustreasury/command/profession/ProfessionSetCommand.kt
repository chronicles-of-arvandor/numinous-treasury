package net.arvandor.numinoustreasury.command.profession

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Sound.ENTITY_VILLAGER_WORK_FLETCHER
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ProfessionSetCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to set your profession")
            return true
        }
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        if (args.size < 1) {
            sender.sendMessage(ChatColor.WHITE.toString() + "Select profession:")
            professionService.professions.forEach { profession ->
                val professionButton = TextComponent("• " + profession.name)
                professionButton.color = ChatColor.GRAY
                professionButton.hoverEvent =
                    HoverEvent(SHOW_TEXT, Text("Click here to set your profession to " + profession.name))
                professionButton.clickEvent = ClickEvent(RUN_COMMAND, "/profession set " + profession.name)
                sender.spigot().sendMessage(professionButton)
            }
            return true
        }
        val profession = professionService.getProfessionByName(args.joinToString(" "))
        if (profession == null) {
            sender.sendMessage(ChatColor.RED.toString() + "There is no profession by that name.")
            return true
        }
        professionService.setProfession(sender, profession) {
            sender.world.playSound(sender.location, ENTITY_VILLAGER_WORK_FLETCHER, 1.0f, 1.0f)
            sender.sendMessage(ChatColor.GREEN.toString() + "Profession set to " + profession.name)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        return if (args.isEmpty()) {
            professionService.professions.map { obj -> obj.name }
        } else {
            professionService.professions
                .map { obj -> obj.name }
                .filter { name ->
                    name.lowercase().startsWith(
                        args.joinToString(" ") { obj: String -> obj.lowercase() },
                    )
                }
        }
    }
}
