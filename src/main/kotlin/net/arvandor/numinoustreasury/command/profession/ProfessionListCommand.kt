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
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ProfessionListCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        val professions = professionService.professions
        sender.sendMessage(ChatColor.WHITE.toString() + "Professions:")
        professions.forEach { profession ->
            val professionButton = TextComponent("• " + profession.name)
            professionButton.color = ChatColor.GRAY
            professionButton.hoverEvent =
                HoverEvent(SHOW_TEXT, Text("Click here to set your profession to " + profession.name))
            professionButton.clickEvent = ClickEvent(RUN_COMMAND, "/profession set " + profession.name)
            sender.spigot().sendMessage(professionButton)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        return listOf()
    }
}
