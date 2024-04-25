package net.arvandor.numinoustreasury.command.profession

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ProfessionViewCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be a player to perform this command.")
            return true
        }
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        val profession = professionService.getProfession(sender)
        if (profession == null) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have a profession.")
            return true
        }
        val level: Int = professionService.getProfessionLevel(sender)
        val experience: Int = professionService.getProfessionExperience(sender)
        val experienceSinceLastLevel =
            experience - (if (level > 1) professionService.getTotalExperienceForLevel(level) else 0)
        val experienceRequiredForNextLevel = professionService.getExperienceForLevel(level + 1)
        val maxLevel = professionService.maxLevel
        sender.sendMessage(ChatColor.WHITE.toString() + "Profession: " + ChatColor.GRAY + profession.name)
        sender.sendMessage(
            ChatColor.WHITE.toString() + "Level: " + ChatColor.GRAY + level +
                (if (level == maxLevel) ChatColor.YELLOW.toString() + " (MAX)" else ""),
        )
        if (level < maxLevel) {
            sender.sendMessage(
                ChatColor.WHITE.toString() + "Experience: " + ChatColor.GRAY + experienceSinceLastLevel +
                    "/" + experienceRequiredForNextLevel,
            )
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
