package net.arvandor.numinoustreasury.command.profession;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;

public final class ProfessionViewCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to perform this command.");
            return true;
        }
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        NuminousProfession profession = professionService.getProfession(player);
        if (profession == null) {
            sender.sendMessage(RED + "You do not have a profession.");
            return true;
        }
        int level = professionService.getProfessionLevel(player);
        int experience = professionService.getProfessionExperience(player);
        int experienceSinceLastLevel = experience - (level > 1 ? professionService.getTotalExperienceForLevel(level) : 0);
        int experienceRequiredForNextLevel = professionService.getExperienceForLevel(level + 1);
        int maxLevel = professionService.getMaxLevel();
        sender.sendMessage(WHITE + "Profession: " + GRAY + profession.getName());
        sender.sendMessage(WHITE + "Level: " + GRAY + level + (level == maxLevel ? YELLOW + " (MAX)" : ""));
        if (level < maxLevel) {
            sender.sendMessage(WHITE + "Experience: " + GRAY + experienceSinceLastLevel + "/" + experienceRequiredForNextLevel);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

}
