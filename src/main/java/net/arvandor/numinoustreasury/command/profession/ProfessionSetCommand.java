package net.arvandor.numinoustreasury.command.profession;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public final class ProfessionSetCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "You must be a player to set your profession");
            return true;
        }
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        if (args.length < 1) {
            sender.sendMessage(WHITE + "Select profession:");
            professionService.getProfessions().forEach(profession -> {
                TextComponent professionButton = new TextComponent("• " + profession.getName());
                professionButton.setColor(GRAY);
                professionButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to set your profession to " + profession.getName())));
                professionButton.setClickEvent(new ClickEvent(RUN_COMMAND, "/profession set " + profession.getName()));
                sender.spigot().sendMessage(professionButton);
            });
            return true;
        }
        NuminousProfession profession = professionService.getProfessionByName(String.join(" ", args));
        if (profession == null) {
            sender.sendMessage(RED + "There is no profession by that name.");
            return true;
        }
        professionService.setProfession(player, profession, () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 1.0f, 1.0f);
            sender.sendMessage(GREEN + "Profession set to " + profession.getName());
        });
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        if (args.length == 0) {
            return professionService.getProfessions().stream().map(NuminousProfession::getName).toList();
        } else {
            return professionService.getProfessions().stream()
                    .map(NuminousProfession::getName)
                    .filter(name -> name.toLowerCase().startsWith(String.join(" ", Arrays.stream(args).map(String::toLowerCase).toList())))
                    .toList();
        }
    }
}
