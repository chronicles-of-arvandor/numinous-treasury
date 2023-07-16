package net.arvandor.numinoustreasury.command.stamina;

import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.ChatColor.RED;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.character.RPKCharacterService;
import com.rpkit.core.service.Services;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService;
import net.arvandor.numinoustreasury.stamina.StaminaTier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class StaminaViewCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    public StaminaViewCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.stamina.view")) {
            sender.sendMessage(RED + "You do not have permission to view your stamina.");
            return true;
        }
        Player target = null;
        if (args.length > 0) {
            target = plugin.getServer().getPlayer(args[0]);
        }
        if (target == null) {
            if (sender instanceof Player player) {
                target = player;
            }
        }
        if (target == null) {
            sender.sendMessage(RED + "You must specify a target if using this command from the console.");
            return true;
        }
        if (target != sender) {
            if (!sender.hasPermission("numinoustreasury.command.stamina.view.other")) {
                sender.sendMessage(RED + "You do not have permission to view other players' stamina.");
                return true;
            }
        }
        NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
        int stamina = staminaService.getStamina(target);
        StaminaTier tier = StaminaTier.forStamina(stamina, staminaService.getMaxStamina());
        if (target == sender) {
            sender.sendMessage(tier.getMessageSelf());
            if (sender.hasPermission("numinoustreasury.command.stamina.view.precise")) {
                sender.sendMessage(GRAY + "You have " + stamina + " stamina.");
            }
        } else {
            RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
            RPKMinecraftProfile minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(target);
            if (minecraftProfile == null) {
                sender.sendMessage(RED + "That player does not have a Minecraft profile.");
                return true;
            }
            RPKCharacterService characterService = Services.INSTANCE.get(RPKCharacterService.class);
            RPKCharacter character = characterService.getPreloadedActiveCharacter(minecraftProfile);
            if (character == null) {
                sender.sendMessage(RED + "That player does not have an active character.");
                return true;
            }
            sender.sendMessage(tier.getMessageOther(character));
            if (sender.hasPermission("numinoustreasury.command.stamina.view.precise")) {
                sender.sendMessage(GRAY + character.getName() + " has " + stamina + " stamina.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.stamina.view.other")) {
            return List.of();
        }
        if (args.length > 0) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }
    }
}
