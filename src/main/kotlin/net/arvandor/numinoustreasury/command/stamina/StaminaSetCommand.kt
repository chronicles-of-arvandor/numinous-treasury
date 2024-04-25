package net.arvandor.numinoustreasury.command.stamina

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.stamina.NuminousStaminaService
import net.arvandor.numinoustreasury.stamina.StaminaTier
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class StaminaSetCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.stamina.set")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to set stamina.")
            return true
        }
        if (args.size < 1) {
            sender.sendMessage(ChatColor.RED.toString() + "Usage: /stamina set (player) [stamina]")
            return true
        }
        var target: Player? = null
        var newStamina = 0
        if (args.size > 1) {
            target = plugin.server.getPlayer(args[0])
            try {
                newStamina = args[1].toInt()
            } catch (exception: NumberFormatException) {
                sender.sendMessage(ChatColor.RED.toString() + "Usage: /stamina set (player) [stamina]")
                return true
            }
        }
        if (target == null) {
            if (sender is Player) {
                target = sender
                try {
                    newStamina = args[0].toInt()
                } catch (exception: NumberFormatException) {
                    sender.sendMessage(ChatColor.RED.toString() + "Usage: /stamina set (player) [stamina]")
                }
            }
        }
        if (target == null) {
            sender.sendMessage(ChatColor.RED.toString() + "You must specify a target if using this command from the console.")
            return true
        }
        if (target !== sender) {
            if (!sender.hasPermission("numinoustreasury.command.stamina.set.other")) {
                sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to set other players' stamina.")
                return true
            }
        }
        val staminaService =
            Services.INSTANCE.get(
                NuminousStaminaService::class.java,
            )
        val oldStamina = staminaService.getStamina(target)
        val maxStamina = staminaService.maxStamina
        if (newStamina > maxStamina) {
            sender.sendMessage(ChatColor.RED.toString() + "Stamina cannot be higher than " + maxStamina + ".")
            return true
        }
        if (newStamina < 0) {
            sender.sendMessage(ChatColor.RED.toString() + "Stamina cannot be lower than 0.")
            return true
        }
        val staminaTransitionMessage = StaminaTier.messageForStaminaTransition(oldStamina, newStamina, maxStamina)
        val finalTarget: Player = target
        val minecraftProfileService =
            Services.INSTANCE.get(
                RPKMinecraftProfileService::class.java,
            )
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(finalTarget)
        if (minecraftProfile == null) {
            sender.sendMessage(ChatColor.RED.toString() + "That player does not have a Minecraft profile.")
            return true
        }
        val characterService =
            Services.INSTANCE.get(
                RPKCharacterService::class.java,
            )
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(ChatColor.RED.toString() + "That player does not have an active character.")
            return true
        }
        val finalNewStamina = newStamina
        staminaService.setStamina(target, newStamina) {
            if (finalTarget === sender) {
                sender.sendMessage(ChatColor.GREEN.toString() + "Stamina set to " + finalNewStamina)
            } else {
                sender.sendMessage(ChatColor.GREEN.toString() + character.name + "'s stamina set to " + finalNewStamina)
            }
            if (staminaTransitionMessage != null) {
                finalTarget.sendMessage(staminaTransitionMessage)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        if (!sender.hasPermission("numinoustreasury.command.stamina.set.other")) {
            return listOf()
        }
        return if (args.size > 0) {
            plugin.server.onlinePlayers
                .map { obj -> obj.name }
                .filter { name ->
                    name.lowercase().startsWith(args[0].lowercase())
                }
        } else {
            plugin.server.onlinePlayers
                .map { obj -> obj.name }
        }
    }
}
