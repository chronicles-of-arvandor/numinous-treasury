package net.arvandor.numinoustreasury.command.node.session

import net.arvandor.numinoustreasury.NuminousTreasury
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NodeSessionCommand(private val plugin: NuminousTreasury) : CommandExecutor, TabCompleter {
    private val nodeSessionNameCommand = NodeSessionNameCommand(plugin)
    private val nodeSessionProfessionCommand = NodeSessionProfessionCommand(plugin)
    private val nodeSessionExperienceCommand = NodeSessionExperienceCommand(plugin)
    private val nodeSessionStaminaCommand = NodeSessionStaminaCommand(plugin)
    private val nodeSessionDropTableCommand = NodeSessionDropTableCommand()
    private val nodeSessionEntranceAreaCommand = NodeSessionEntranceAreaCommand()
    private val nodeSessionEntranceDestinationCommand = NodeSessionEntranceDestinationCommand()
    private val nodeSessionExitAreaCommand = NodeSessionExitAreaCommand()
    private val nodeSessionExitDestinationCommand = NodeSessionExitDestinationCommand()
    private val nodeSessionAreaCommand = NodeSessionAreaCommand()
    private val nodeSessionCreateCommand = NodeSessionCreateCommand()

    private val nameAliases = listOf("name")
    private val professionAliases = listOf("profession", "prof")
    private val experienceAliases = listOf("experience", "exp")
    private val staminaAliases = listOf("stamina", "staminacost")
    private val dropTableAliases = listOf("droptable", "drops")
    private val entranceAreaAliases = listOf("entrancearea")
    private val entranceDestinationAliases = listOf("entrancedestination", "entrancedest")
    private val exitAreaAliases = listOf("exitarea")
    private val exitDestinationAliases = listOf("exitdestination", "exitdest")
    private val areaAliases = listOf("area")
    private val createAliases = listOf("create")
    private val subcommands =
        nameAliases +
            professionAliases +
            experienceAliases +
            staminaAliases +
            dropTableAliases +
            entranceAreaAliases +
            entranceDestinationAliases +
            exitAreaAliases +
            exitDestinationAliases +
            areaAliases +
            createAliases

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(
                ChatColor.RED.toString() + "Usage: /node session [${subcommands.joinToString("|")}]",
            )
            return true
        }
        when (args[0].lowercase()) {
            in nameAliases -> {
                return nodeSessionNameCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in professionAliases -> {
                return nodeSessionProfessionCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in experienceAliases -> {
                return nodeSessionExperienceCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in staminaAliases -> {
                return nodeSessionStaminaCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in dropTableAliases -> {
                return nodeSessionDropTableCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in entranceAreaAliases -> {
                return nodeSessionEntranceAreaCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in entranceDestinationAliases -> {
                return nodeSessionEntranceDestinationCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in exitAreaAliases -> {
                return nodeSessionExitAreaCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in exitDestinationAliases -> {
                return nodeSessionExitDestinationCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in areaAliases -> {
                return nodeSessionAreaCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            in createAliases -> {
                return nodeSessionCreateCommand.onCommand(
                    sender,
                    command,
                    label,
                    args.drop(1).toTypedArray(),
                )
            }
            else -> {
                sender.sendMessage(
                    ChatColor.RED.toString() + "Usage: /node session [${subcommands.joinToString("|")}]",
                )
                return true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): List<String>? {
        return when {
            args.isEmpty() -> subcommands
            args.size == 1 ->
                subcommands
                    .filter { subcommand: String -> subcommand.startsWith(args[0].lowercase()) }
            else ->
                when {
                    nameAliases.contains(args[0].lowercase()) ->
                        nodeSessionNameCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    professionAliases.contains(args[0].lowercase()) ->
                        nodeSessionProfessionCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    experienceAliases.contains(args[0].lowercase()) ->
                        nodeSessionExperienceCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    staminaAliases.contains(args[0].lowercase()) ->
                        nodeSessionStaminaCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    dropTableAliases.contains(args[0].lowercase()) ->
                        nodeSessionDropTableCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    entranceAreaAliases.contains(args[0].lowercase()) ->
                        nodeSessionEntranceAreaCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    entranceDestinationAliases.contains(args[0].lowercase()) ->
                        nodeSessionEntranceDestinationCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    exitAreaAliases.contains(args[0].lowercase()) ->
                        nodeSessionExitAreaCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    exitDestinationAliases.contains(args[0].lowercase()) ->
                        nodeSessionExitDestinationCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    areaAliases.contains(args[0].lowercase()) ->
                        nodeSessionAreaCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    createAliases.contains(args[0].lowercase()) ->
                        nodeSessionCreateCommand.onTabComplete(
                            sender,
                            command,
                            label,
                            args.drop(1).toTypedArray(),
                        )
                    else -> listOf<String>()
                }
        }
    }
}
