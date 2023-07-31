package net.arvandor.numinoustreasury.command.profession.experience;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.ChatColor.YELLOW;

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
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class ProfessionExperienceAddCommand implements CommandExecutor, TabCompleter {

    private final NuminousTreasury plugin;

    public ProfessionExperienceAddCommand(NuminousTreasury plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(RED + "Usage: /profession experience add [player] [experience]");
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

            professionService.addProfessionExperience(target, experience, (oldExperience, newExperience) -> {
                sender.sendMessage(GREEN + "Gave " + character.getName() + " " + (newExperience - oldExperience) + " " + profession.getName() + " experience.");
                Player onlineTarget = target.getPlayer();
                if (onlineTarget != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        int oldLevel = professionService.getLevelAtExperience(oldExperience);
                        int newLevel = professionService.getLevelAtExperience(newExperience);
                        int experienceSinceLastLevel = newExperience - (newLevel > 1 ? professionService.getTotalExperienceForLevel(newLevel) : 0);
                        int experienceRequiredForNextLevel = professionService.getExperienceForLevel(newLevel + 1);
                        int maxLevel = professionService.getMaxLevel();
                        if (newLevel < maxLevel) {
                            onlineTarget.sendMessage(YELLOW + "+" + (newExperience - oldExperience) + " " + profession.getName() + " exp (" + experienceSinceLastLevel + "/" + experienceRequiredForNextLevel + ")");
                        } else if (oldLevel < maxLevel && newLevel == maxLevel) {
                            onlineTarget.sendMessage(YELLOW + "+" + (newExperience - oldExperience) + " " + profession.getName() + "exp (MAX LEVEL)");
                        }
                        if (newLevel > oldLevel) {
                            if (newLevel == maxLevel) {
                                onlineTarget.playSound(onlineTarget.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                            } else {
                                onlineTarget.playSound(onlineTarget.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            }
                            onlineTarget.sendMessage(YELLOW + "Level up! You are now a level " + newLevel + " " + profession.getName());
                        } else {
                            onlineTarget.playSound(onlineTarget.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }
                    });
                } else {
                    RPKThinProfile thinProfile = minecraftProfile.getProfile();
                    if (thinProfile instanceof RPKProfile profile) {
                        notificationService.createNotification(
                                profile,
                                "Profession experience granted",
                                "Your " + profession.getName() + " experience has increased by " + experience + "."
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
