package net.arvandor.numinoustreasury.command.profession;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.ChatColor.WHITE;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public final class ProfessionListCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        List<NuminousProfession> professions = professionService.getProfessions();
        sender.sendMessage(WHITE + "Professions:");
        professions.forEach(profession -> {
            TextComponent professionButton = new TextComponent("• " + profession.getName());
            professionButton.setColor(GRAY);
            professionButton.setHoverEvent(new HoverEvent(SHOW_TEXT, new Text("Click here to set your profession to " + profession.getName())));
            professionButton.setClickEvent(new ClickEvent(RUN_COMMAND, "/profession set " + profession.getName()));
            sender.spigot().sendMessage(professionButton);
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
