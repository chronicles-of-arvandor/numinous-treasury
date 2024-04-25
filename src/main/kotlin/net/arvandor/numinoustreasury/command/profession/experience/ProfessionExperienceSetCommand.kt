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
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class ProfessionExperienceSetCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.profession.experience.set")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to perform this command.")
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /profession experience set [player] [experience]")
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
                professionService.setProfessionExperience(target, experience) {
                    sender.sendMessage(
                        ChatColor.GREEN.toString() + "Set " + character.name + "'s " + profession.name +
                            " experience to " + experience + ".",
                    )
                    val onlineTarget = target.player
                    if (onlineTarget != null) {
                        onlineTarget.sendMessage(
                            ChatColor.GREEN.toString() + "Your " + profession.name + " experience has been set to " +
                                experience + ".",
                        )
                    } else {
                        val thinProfile = minecraftProfile.profile
                        if (thinProfile is RPKProfile) {
                            notificationService.createNotification(
                                thinProfile,
                                "Profession experience modified",
                                "Your " + profession.name + " experience has been set to " + experience + ".",
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
    ): List<String>? {
        if (args.isEmpty()) {
            return plugin.server.offlinePlayers.mapNotNull { obj: OfflinePlayer -> obj.name }
        } else if (args.size == 1) {
            return plugin.server.offlinePlayers
                .mapNotNull { obj: OfflinePlayer -> obj.name }
                .filter { name: String ->
                    name.lowercase().startsWith(args[0].lowercase())
                }
        } else if (args.size == 2) {
            val professionService =
                Services.INSTANCE.get(
                    NuminousProfessionService::class.java,
                )
            val maxExp = professionService.getTotalExperienceForLevel(professionService.maxLevel)
            return (0 until maxExp).map { i: Int -> i.toString() }
                .filter { i: String -> i.startsWith(args[1]) }
        } else {
            return listOf()
        }
    }
}
