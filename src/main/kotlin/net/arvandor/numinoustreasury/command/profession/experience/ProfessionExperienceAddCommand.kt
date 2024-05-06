package net.arvandor.numinoustreasury.command.profession.experience

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import net.md_5.bungee.api.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP
import org.bukkit.Sound.ENTITY_PLAYER_LEVELUP
import org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ProfessionExperienceAddCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.profession.experience.add")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to perform this command.")
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /profession experience add [player] [experience]")
            return true
        }
        val target = plugin.server.getOfflinePlayer(args[0])

        val experience: Int
        try {
            experience = args[1].toInt()
        } catch (exception: NumberFormatException) {
            sender.sendMessage(ChatColor.RED.toString() + "Experience must be an integer.")
            return true
        }
        val minecraftProfileService =
            Services.INSTANCE.get(
                RPKMinecraftProfileService::class.java,
            )
        val characterService =
            Services.INSTANCE.get(
                RPKCharacterService::class.java,
            )
        val notificationService =
            Services.INSTANCE.get(
                RPKNotificationService::class.java,
            )
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
                if (minecraftProfile == null) {
                    sender.sendMessage(ChatColor.RED.toString() + "That player does not have a Minecraft profile.")
                    return@Runnable
                }
                val character = characterService.getActiveCharacter(minecraftProfile).join()
                if (character == null) {
                    sender.sendMessage(ChatColor.RED.toString() + "That player does not have an active character.")
                    return@Runnable
                }

                val profession = professionService.getProfession(target)
                if (profession == null) {
                    sender.sendMessage(ChatColor.RED.toString() + "That player does not have a profession.")
                    return@Runnable
                }
                professionService.addProfessionExperience(target, experience) { oldExperience: Int, newExperience: Int ->
                    sender.sendMessage(
                        ChatColor.GREEN.toString() + "Gave " + character.name + " " + (newExperience - oldExperience) +
                            " " + profession.name + " experience.",
                    )
                    val onlineTarget = target.player
                    if (onlineTarget != null) {
                        plugin.server.scheduler.runTask(
                            plugin,
                            Runnable {
                                val oldLevel = professionService.getLevelAtExperience(oldExperience)
                                val newLevel = professionService.getLevelAtExperience(newExperience)
                                val experienceSinceLastLevel =
                                    newExperience - (if (newLevel > 1) professionService.getTotalExperienceForLevel(newLevel) else 0)
                                val experienceRequiredForNextLevel = professionService.getExperienceForLevel(newLevel + 1)
                                val maxLevel = professionService.maxLevel
                                if (newLevel < maxLevel) {
                                    onlineTarget.sendMessage(
                                        ChatColor.YELLOW.toString() + "+" + (newExperience - oldExperience) + " " +
                                            profession.name + " exp (" +
                                            experienceSinceLastLevel + "/" + experienceRequiredForNextLevel + ")",
                                    )
                                } else if (oldLevel < maxLevel && newLevel == maxLevel) {
                                    onlineTarget.sendMessage(
                                        ChatColor.YELLOW.toString() + "+" + (newExperience - oldExperience) + " " +
                                            profession.name + " exp (MAX LEVEL)",
                                    )
                                }
                                if (newLevel > oldLevel) {
                                    if (newLevel == maxLevel) {
                                        onlineTarget.world.playSound(
                                            onlineTarget.location,
                                            UI_TOAST_CHALLENGE_COMPLETE,
                                            1.0f,
                                            1.0f,
                                        )
                                    } else {
                                        onlineTarget.world.playSound(onlineTarget.location, ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
                                    }
                                    onlineTarget.sendMessage(
                                        ChatColor.YELLOW.toString() + "Level up! You are now a level " +
                                            newLevel + " " + profession.name,
                                    )
                                } else {
                                    onlineTarget.world.playSound(
                                        onlineTarget.location,
                                        ENTITY_EXPERIENCE_ORB_PICKUP,
                                        1.0f,
                                        1.0f,
                                    )
                                }
                            },
                        )
                    } else {
                        val thinProfile = minecraftProfile.profile
                        if (thinProfile is RPKProfile) {
                            notificationService.createNotification(
                                thinProfile,
                                "Profession experience granted",
                                "Your " + profession.name + " experience has increased by " + experience + ".",
                            )
                        }
                    }
                }
            },
        )

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        when {
            args.isEmpty() -> return plugin.server.offlinePlayers.mapNotNull { obj: OfflinePlayer -> obj.name }
            args.size == 1 ->
                return plugin.server.offlinePlayers
                    .mapNotNull { obj -> obj.name }
                    .filter { name -> name.lowercase().startsWith(args[0].lowercase()) }
            args.size == 2 -> {
                val professionService =
                    Services.INSTANCE.get(
                        NuminousProfessionService::class.java,
                    )
                val maxExp = professionService.getTotalExperienceForLevel(professionService.maxLevel)
                return (0 until maxExp).map { i: Int -> i.toString() }
                    .filter { i: String -> i.startsWith(args[1]) }
            }
            else -> return listOf()
        }
    }
}
