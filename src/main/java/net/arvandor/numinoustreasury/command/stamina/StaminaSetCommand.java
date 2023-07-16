package net.arvandor.numinoustreasury.command.stamina;

import static net.md_5.bungee.api.ChatColor.GREEN;
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

public class StaminaSetCommand implements CommandExecutor, TabCompleter {
    private final NuminousTreasury plugin;

    public StaminaSetCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.stamina.set")) {
            sender.sendMessage(RED + "You do not have permission to set stamina.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(RED + "Usage: /stamina set (player) [stamina]");
            return true;
        }
        Player target = null;
        int newStamina = 0;
        if (args.length > 1) {
            target = plugin.getServer().getPlayer(args[0]);
            try {
                newStamina = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(RED + "Usage: /stamina set (player) [stamina]");
                return true;
            }
        }
        if (target == null) {
            if (sender instanceof Player player) {
                target = player;
                try {
                    newStamina = Integer.parseInt(args[0]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(RED + "Usage: /stamina set (player) [stamina]");
                }
            }
        }
        if (target == null) {
            sender.sendMessage(RED + "You must specify a target if using this command from the console.");
            return true;
        }
        if (target != sender) {
            if (!sender.hasPermission("numinoustreasury.command.stamina.set.other")) {
                sender.sendMessage(RED + "You do not have permission to set other players' stamina.");
                return true;
            }
        }
        NuminousStaminaService staminaService = Services.INSTANCE.get(NuminousStaminaService.class);
        int oldStamina = staminaService.getStamina(target);
        int maxStamina = staminaService.getMaxStamina();
        if (newStamina > maxStamina) {
            sender.sendMessage(RED + "Stamina cannot be higher than " + maxStamina + ".");
            return true;
        }
        if (newStamina < 0) {
            sender.sendMessage(RED + "Stamina cannot be lower than 0.");
            return true;
        }
        String staminaTransitionMessage = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, maxStamina);
        final Player finalTarget = target;
        RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
        RPKMinecraftProfile minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(finalTarget);
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
        final int finalNewStamina = newStamina;
        staminaService.setStamina(target, newStamina, () -> {
            if (finalTarget == sender) {
                sender.sendMessage(GREEN + "Stamina set to " + finalNewStamina);
            } else {
                sender.sendMessage(GREEN + character.getName() + "'s stamina set to " + finalNewStamina);
            }
            if (staminaTransitionMessage != null) {
                finalTarget.sendMessage(staminaTransitionMessage);
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.stamina.set.other")) {
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
