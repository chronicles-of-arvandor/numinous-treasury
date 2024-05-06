package net.arvandor.numinoustreasury.node

import net.arvandor.numinoustreasury.droptable.NuminousDropTable
import net.arvandor.numinoustreasury.profession.NuminousProfession
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Location
import org.bukkit.entity.Player

class NuminousNodeCreationSession {
    var name: String = ""
    val requiredProfessionLevel: MutableMap<NuminousProfession, Int> = mutableMapOf()
    var experience: Int = 0
    var staminaCost: Int = 0
    var dropTable: NuminousDropTable? = null
    var entranceAreaLocation1: Location? = null
    var entranceAreaLocation2: Location? = null
    var entranceWarpDestination: Location? = null
    var exitAreaLocation1: Location? = null
    var exitAreaLocation2: Location? = null
    var exitWarpDestination: Location? = null
    var areaLocation1: Location? = null
    var areaLocation2: Location? = null

    fun addRequiredProfessionLevel(
        profession: NuminousProfession,
        level: Int,
    ) {
        requiredProfessionLevel[profession] = level
    }

    fun removeRequiredProfessionLevel(profession: NuminousProfession) {
        requiredProfessionLevel.remove(profession)
    }

    val isValid: Boolean
        get() =
            (
                name.isNotBlank() &&
                    requiredProfessionLevel.isNotEmpty()
            ) && dropTable != null &&
                entranceAreaLocation1 != null &&
                entranceAreaLocation2 != null &&
                entranceAreaLocation1!!.world === entranceAreaLocation2!!.world &&
                entranceWarpDestination != null &&
                exitAreaLocation1 != null &&
                exitAreaLocation2 != null &&
                exitAreaLocation1!!.world === exitAreaLocation2!!.world &&
                exitWarpDestination != null &&
                areaLocation1 != null &&
                areaLocation2 != null &&
                areaLocation1!!.world === areaLocation2!!.world

    fun display(player: Player) {
        player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Node setup")
        player.spigot().sendMessage(
            *ComponentBuilder("Name: ")
                .color(ChatColor.WHITE)
                .append(name)
                .color(ChatColor.GRAY)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session name",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the name of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.sendMessage(ChatColor.WHITE.toString() + "Required profession:")
        requiredProfessionLevel.forEach { (profession: NuminousProfession, level: Int) ->
            player.spigot().sendMessage(
                *ComponentBuilder("• Lv" + level + " " + profession.name)
                    .color(ChatColor.GRAY)
                    .append(" (Delete)")
                    .color(ChatColor.RED)
                    .event(
                        ClickEvent(
                            RUN_COMMAND,
                            "/node session profession remove " + profession.id,
                        ),
                    )
                    .event(
                        HoverEvent(
                            SHOW_TEXT,
                            Text(
                                ComponentBuilder("Click here to remove " + profession.name + " from the applicable professions.")
                                    .color(ChatColor.RED)
                                    .create(),
                            ),
                        ),
                    )
                    .create(),
            )
        }
        player.spigot().sendMessage(
            *ComponentBuilder("Add profession")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session profession add",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to add a profession to the applicable professions.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Experience: ")
                .color(ChatColor.WHITE)
                .append(experience.toString())
                .color(ChatColor.GRAY)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session experience",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the experience granted by the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Stamina cost: ")
                .color(ChatColor.WHITE)
                .append(staminaCost.toString())
                .color(ChatColor.GRAY)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session stamina",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the stamina cost of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Drop table: ")
                .color(ChatColor.WHITE)
                .append(if (dropTable != null) dropTable?.id else "unset")
                .color(if (dropTable != null) ChatColor.GREEN else ChatColor.RED)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session droptable",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the drop table of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Entrance area: ")
                .color(ChatColor.WHITE)
                .append(if (entranceAreaLocation1 != null && entranceAreaLocation2 != null) "set" else "unset")
                .color(if (entranceAreaLocation1 != null && entranceAreaLocation2 != null) ChatColor.GREEN else ChatColor.RED)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session entrancearea",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the entrance area of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Entrance warp destination: ")
                .color(ChatColor.WHITE)
                .append(if (entranceWarpDestination != null) "set" else "unset")
                .color(if (entranceWarpDestination != null) ChatColor.GREEN else ChatColor.RED)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session entrancedest",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the entrance destination of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Exit area: ")
                .color(ChatColor.WHITE)
                .append(if (exitAreaLocation1 != null && exitAreaLocation2 != null) "set" else "unset")
                .color(if (exitAreaLocation1 != null && exitAreaLocation2 != null) ChatColor.GREEN else ChatColor.RED)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session exitarea",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the exit area of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Exit warp destination: ")
                .color(ChatColor.WHITE)
                .append(if (exitWarpDestination != null) "set" else "unset")
                .color(if (exitWarpDestination != null) ChatColor.GREEN else ChatColor.RED)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session exitdest",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the exit destination of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        player.spigot().sendMessage(
            *ComponentBuilder("Area: ")
                .color(ChatColor.WHITE)
                .append(if (areaLocation1 != null && areaLocation2 != null) "set" else "unset")
                .color(if (areaLocation1 != null && areaLocation2 != null) ChatColor.GREEN else ChatColor.RED)
                .append(" (Edit)")
                .color(ChatColor.GREEN)
                .event(
                    ClickEvent(
                        RUN_COMMAND,
                        "/node session area",
                    ),
                )
                .event(
                    HoverEvent(
                        SHOW_TEXT,
                        Text(
                            ComponentBuilder("Click here to set the area of the node.")
                                .color(ChatColor.GREEN)
                                .create(),
                        ),
                    ),
                )
                .create(),
        )
        if (isValid) {
            player.spigot().sendMessage(
                *ComponentBuilder("Create")
                    .color(ChatColor.GREEN)
                    .event(
                        ClickEvent(
                            RUN_COMMAND,
                            "/node session create",
                        ),
                    )
                    .event(
                        HoverEvent(
                            SHOW_TEXT,
                            Text(
                                ComponentBuilder("Click here to create the node")
                                    .color(ChatColor.GREEN)
                                    .create(),
                            ),
                        ),
                    )
                    .create(),
            )
        } else {
            player.sendMessage(ChatColor.GRAY.toString() + "All fields must be completed before the node may be created.")
        }
    }
}
