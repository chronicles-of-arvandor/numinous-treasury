package net.kingdommc.darkages.numinoustreasury.node;

import net.kingdommc.darkages.numinoustreasury.droptable.NuminousDropTable;
import net.kingdommc.darkages.numinoustreasury.profession.NuminousProfession;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public final class NuminousNodeCreationSession {
    private String name;
    private Map<NuminousProfession, Integer> requiredProfessionLevel;
    private int experience;
    private int staminaCost;
    private NuminousDropTable dropTable;
    private Location entranceAreaLocation1;
    private Location entranceAreaLocation2;
    private Location entranceWarpDestination;
    private Location exitAreaLocation1;
    private Location exitAreaLocation2;
    private Location exitWarpDestination;
    private Location areaLocation1;
    private Location areaLocation2;

    public NuminousNodeCreationSession() {
        this.name = "";
        this.requiredProfessionLevel = new HashMap<>();
        this.experience = 0;
        this.staminaCost = 0;
        this.dropTable = null;
        this.entranceAreaLocation1 = null;
        this.entranceAreaLocation2 = null;
        this.entranceWarpDestination = null;
        this.exitAreaLocation1 = null;
        this.exitAreaLocation2 = null;
        this.exitWarpDestination = null;
        this.areaLocation1 = null;
        this.areaLocation2 = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<NuminousProfession, Integer> getRequiredProfessionLevel() {
        return requiredProfessionLevel;
    }

    public void addRequiredProfessionLevel(NuminousProfession profession, int level) {
        requiredProfessionLevel.put(profession, level);
    }

    public void removeRequiredProfessionLevel(NuminousProfession profession) {
        requiredProfessionLevel.remove(profession);
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getStaminaCost() {
        return staminaCost;
    }

    public void setStaminaCost(int staminaCost) {
        this.staminaCost = staminaCost;
    }

    public NuminousDropTable getDropTable() {
        return dropTable;
    }

    public void setDropTable(NuminousDropTable dropTable) {
        this.dropTable = dropTable;
    }

    public Location getEntranceAreaLocation1() {
        return entranceAreaLocation1;
    }

    public void setEntranceAreaLocation1(Location entranceAreaLocation1) {
        this.entranceAreaLocation1 = entranceAreaLocation1;
    }

    public Location getEntranceAreaLocation2() {
        return entranceAreaLocation2;
    }

    public void setEntranceAreaLocation2(Location entranceAreaLocation2) {
        this.entranceAreaLocation2 = entranceAreaLocation2;
    }

    public Location getEntranceWarpDestination() {
        return entranceWarpDestination;
    }

    public void setEntranceWarpDestination(Location entranceWarpDestination) {
        this.entranceWarpDestination = entranceWarpDestination;
    }

    public Location getExitAreaLocation1() {
        return exitAreaLocation1;
    }

    public void setExitAreaLocation1(Location exitAreaLocation1) {
        this.exitAreaLocation1 = exitAreaLocation1;
    }

    public Location getExitAreaLocation2() {
        return exitAreaLocation2;
    }

    public void setExitAreaLocation2(Location exitAreaLocation2) {
        this.exitAreaLocation2 = exitAreaLocation2;
    }

    public Location getExitWarpDestination() {
        return exitWarpDestination;
    }

    public void setExitWarpDestination(Location exitWarpDestination) {
        this.exitWarpDestination = exitWarpDestination;
    }

    public Location getAreaLocation1() {
        return areaLocation1;
    }

    public void setAreaLocation1(Location areaLocation1) {
        this.areaLocation1 = areaLocation1;
    }

    public Location getAreaLocation2() {
        return areaLocation2;
    }

    public void setAreaLocation2(Location areaLocation2) {
        this.areaLocation2 = areaLocation2;
    }

    public boolean isValid() {
        return !name.isBlank()
                && !requiredProfessionLevel.isEmpty()
                && dropTable != null
                && entranceAreaLocation1 != null
                && entranceAreaLocation2 != null
                && entranceAreaLocation1.getWorld() == entranceAreaLocation2.getWorld()
                && entranceWarpDestination != null
                && exitAreaLocation1 != null
                && exitAreaLocation2 != null
                && exitAreaLocation1.getWorld() == exitAreaLocation2.getWorld()
                && exitWarpDestination != null
                && areaLocation1 != null
                && areaLocation2 != null
                && areaLocation1.getWorld() == areaLocation2.getWorld();
    }

    public void display(Player player) {
        player.sendMessage(WHITE + BOLD.toString() + "Node setup");
        player.spigot().sendMessage(
            new ComponentBuilder("Name: ")
                    .color(WHITE)
                    .append(getName())
                    .color(GRAY)
                    .append(" (Edit)")
                    .color(GREEN)
                    .event(new ClickEvent(
                            RUN_COMMAND,
                            "/node session name"
                    ))
                    .event(new HoverEvent(
                            SHOW_TEXT,
                            new Text(
                                    new ComponentBuilder("Click here to set the name of the node.")
                                            .color(GREEN)
                                            .create()
                            )
                    ))
                    .create()
        );
        player.sendMessage(WHITE + "Required profession:");
        getRequiredProfessionLevel().forEach((profession, level) -> {
            player.spigot().sendMessage(
                    new ComponentBuilder("• Lv" + level + " " + profession.getName())
                            .color(GRAY)
                            .append(" (Delete)")
                            .color(RED)
                            .event(new ClickEvent(
                                    RUN_COMMAND,
                                    "/node session profession remove " + profession.getId()
                            ))
                            .event(new HoverEvent(
                                    SHOW_TEXT,
                                    new Text(
                                            new ComponentBuilder("Click here to remove " + profession.getName() + " from the applicable professions.")
                                                    .color(RED)
                                                    .create()
                                    )
                            ))
                            .create()
            );
        });
        player.spigot().sendMessage(
                new ComponentBuilder("Add profession")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session profession add"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to add a profession to the applicable professions.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Experience: ")
                        .color(WHITE)
                        .append(Integer.toString(getExperience()))
                        .color(GRAY)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session experience"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the experience granted by the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Stamina cost: ")
                        .color(WHITE)
                        .append(Integer.toString(getStaminaCost()))
                        .color(GRAY)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session stamina"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the stamina cost of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Drop table: ")
                        .color(WHITE)
                        .append(dropTable != null ? dropTable.getId() : "unset")
                        .color(dropTable != null ? GREEN : RED)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session droptable"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the drop table of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Entrance area: ")
                        .color(WHITE)
                        .append(entranceAreaLocation1 != null && entranceAreaLocation2 != null ? "set" : "unset")
                        .color(entranceAreaLocation1 != null && entranceAreaLocation2 != null ? GREEN : RED)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session entrancearea"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the entrance area of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Entrance warp destination: ")
                        .color(WHITE)
                        .append(entranceWarpDestination != null ? "set" : "unset")
                        .color(entranceWarpDestination != null ? GREEN : RED)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session entrancedest"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the entrance destination of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Exit area: ")
                        .color(WHITE)
                        .append(exitAreaLocation1 != null && exitAreaLocation2 != null ? "set" : "unset")
                        .color(exitAreaLocation1 != null && exitAreaLocation2 != null ? GREEN : RED)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session exitarea"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the exit area of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Exit warp destination: ")
                        .color(WHITE)
                        .append(exitWarpDestination != null ? "set" : "unset")
                        .color(exitWarpDestination != null ? GREEN : RED)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session exitdest"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the exit destination of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        player.spigot().sendMessage(
                new ComponentBuilder("Area: ")
                        .color(WHITE)
                        .append(areaLocation1 != null && areaLocation2 != null ? "set" : "unset")
                        .color(areaLocation1 != null && areaLocation2 != null ? GREEN : RED)
                        .append(" (Edit)")
                        .color(GREEN)
                        .event(new ClickEvent(
                                RUN_COMMAND,
                                "/node session area"
                        ))
                        .event(new HoverEvent(
                                SHOW_TEXT,
                                new Text(
                                        new ComponentBuilder("Click here to set the area of the node.")
                                                .color(GREEN)
                                                .create()
                                )
                        ))
                        .create()
        );
        if (isValid()) {
            player.spigot().sendMessage(
                    new ComponentBuilder("Create")
                            .color(GREEN)
                            .event(new ClickEvent(
                                    RUN_COMMAND,
                                    "/node session create"
                            ))
                            .event(new HoverEvent(
                                    SHOW_TEXT,
                                    new Text(
                                            new ComponentBuilder("Click here to create the node")
                                                    .color(GREEN)
                                                    .create()
                                    )
                            ))
                            .create()
            );
        } else {
            player.sendMessage(GRAY + "All fields must be completed before the node may be created.");
        }
    }
}
