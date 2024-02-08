package net.arvandor.numinoustreasury.command.profession.experience;

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;

import com.rpkit.characters.bukkit.character.RPKCharacter;
import com.rpkit.characters.bukkit.character.RPKCharacterService;
import com.rpkit.core.service.Services;
import com.rpkit.notifications.bukkit.notification.RPKNotificationService;
import com.rpkit.players.bukkit.profile.RPKProfile;
import com.rpkit.players.bukkit.profile.RPKThinProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile;
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class ProfessionExperienceSetCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    public ProfessionExperienceSetCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("numinoustreasury.command.profession.experience.set")) {
            sender.sendMessage(RED + "You do not have permission to perform this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(RED + "Usage: /profession experience set [player] [experience]");
            return true;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);

        int experience;
        try {
            experience = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(RED + "Experience must be an integer.");
            return true;
        }
        RPKMinecraftProfileService minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService.class);
        RPKCharacterService characterService = Services.INSTANCE.get(RPKCharacterService.class);
        RPKNotificationService notificationService = Services.INSTANCE.get(RPKNotificationService.class);
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            RPKMinecraftProfile minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join();
            if (minecraftProfile == null) {
                sender.sendMessage(RED + "That player does not have a Minecraft profile.");
                return;
            }
            RPKCharacter character = characterService.getActiveCharacter(minecraftProfile).join();
            if (character == null) {
                sender.sendMessage(RED + "That player does not have an active character.");
                return;
            }

            NuminousProfession profession = professionService.getProfession(target);
            if (profession == null) {
                sender.sendMessage(RED + "That player does not have a profession.");
                return;
            }

            professionService.setProfessionExperience(target, experience, () -> {
                sender.sendMessage(GREEN + "Set " + character.getName() + "'s " + profession.getName() + " experience to " + experience + ".");
                Player onlineTarget = target.getPlayer();
                if (onlineTarget != null) {
                    onlineTarget.sendMessage(GREEN + "Your " + profession.getName() + " experience has been set to " + experience + ".");
                } else {
                    RPKThinProfile thinProfile = minecraftProfile.getProfile();
                    if (thinProfile instanceof RPKProfile profile) {
                        notificationService.createNotification(
                                profile,
                                "Profession experience modified",
                                "Your " + profession.getName() + " experience has been set to " + experience + "."
                        );
                    }
                }
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers()).map(OfflinePlayer::getName).toList();
        } else if (args.length == 1) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
            int maxExp = professionService.getTotalExperienceForLevel(professionService.getMaxLevel());
            return IntStream.range(0, maxExp).mapToObj(Integer::toString).filter(i -> i.startsWith(args[1])).toList();
        } else {
            return List.of();
        }
    }
}
