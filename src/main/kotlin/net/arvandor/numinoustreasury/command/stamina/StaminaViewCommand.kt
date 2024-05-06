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

class StaminaViewCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!sender.hasPermission("numinoustreasury.command.stamina.view")) {
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to view your stamina.")
            return true
        }
        var target: Player? = null
        if (args.size > 0) {
            target = plugin.server.getPlayer(args[0])
        }
        if (target == null) {
            if (sender is Player) {
                target = sender
            }
        }
        if (target == null) {
            sender.sendMessage(ChatColor.RED.toString() + "You must specify a target if using this command from the console.")
            return true
        }
        if (target !== sender) {
            if (!sender.hasPermission("numinoustreasury.command.stamina.view.other")) {
                sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to view other players' stamina.")
                return true
            }
        }
        val staminaService =
            Services.INSTANCE.get(
                NuminousStaminaService::class.java,
            )
        val stamina = staminaService.getStamina(target)
        val tier = StaminaTier.forStamina(stamina, staminaService.maxStamina)
        if (target === sender) {
            sender.sendMessage(tier.messageSelf)
            if (sender.hasPermission("numinoustreasury.command.stamina.view.precise")) {
                sender.sendMessage(ChatColor.GRAY.toString() + "You have " + stamina + " stamina.")
            }
        } else {
            val minecraftProfileService =
                Services.INSTANCE.get(
                    RPKMinecraftProfileService::class.java,
                )
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(target)
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
            sender.sendMessage(tier.getMessageOther(character))
            if (sender.hasPermission("numinoustreasury.command.stamina.view.precise")) {
                sender.sendMessage(ChatColor.GRAY.toString() + character.name + " has " + stamina + " stamina.")
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
        if (!sender.hasPermission("numinoustreasury.command.stamina.view.other")) {
            return listOf()
        }
        return if (args.isNotEmpty()) {
            plugin.server.onlinePlayers
                .map { obj -> obj.name }
                .filter { name: String ->
                    name.lowercase().startsWith(args[0].lowercase())
                }
        } else {
            plugin.server.onlinePlayers
                .map { obj -> obj.name }
        }
    }
}
