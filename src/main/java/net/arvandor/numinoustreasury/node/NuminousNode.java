package net.arvandor.numinoustreasury.node;

import net.arvandor.numinoustreasury.area.Area;
import net.arvandor.numinoustreasury.droptable.NuminousDropTable;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public final class NuminousNode {

    private final String id;
    private final String name;
    private final Map<NuminousProfession, Integer> requiredProfessionLevel;
    private final int experience;
    private final int staminaCost;
    private final NuminousDropTable dropTable;
    private final Area entranceArea;
    private final Location entranceWarpDestination;
    private final Area exitArea;
    private final Location exitWarpDestination;
    private final Area area;

    public NuminousNode(String id, String name, Map<NuminousProfession, Integer> requiredProfessionLevel, int experience, int staminaCost, NuminousDropTable dropTable, Area entranceArea, Location entranceWarpDestination, Area exitArea, Location exitWarpDestination, Area area) {
        this.id = id;
        this.name = name;
        this.requiredProfessionLevel = requiredProfessionLevel;
        this.experience = experience;
        this.staminaCost = staminaCost;
        this.dropTable = dropTable;
        this.entranceArea = entranceArea;
        this.entranceWarpDestination = entranceWarpDestination;
        this.exitArea = exitArea;
        this.exitWarpDestination = exitWarpDestination;
        this.area = area;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<NuminousProfession> getApplicableProfessions() {
        return requiredProfessionLevel.keySet().stream().toList();
    }

    public Integer getRequiredProfessionLevel(NuminousProfession profession) {
        return requiredProfessionLevel.get(profession);
    }

    public int getExperience() {
        return experience;
    }

    public int getStaminaCost() {
        return staminaCost;
    }

    public NuminousDropTable getDropTable() {
        return dropTable;
    }

    public Area getEntranceArea() {
        return entranceArea;
    }

    public Location getEntranceWarpDestination() {
        return entranceWarpDestination;
    }

    public Area getExitArea() {
        return exitArea;
    }

    public Location getExitWarpDestination() {
        return exitWarpDestination;
    }

    public Area getArea() {
        return area;
    }
}
