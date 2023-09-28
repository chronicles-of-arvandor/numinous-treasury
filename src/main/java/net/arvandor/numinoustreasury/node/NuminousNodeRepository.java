package net.arvandor.numinoustreasury.node;

import com.rpkit.core.service.Services;
import net.arvandor.numinoustreasury.area.Area;
import net.arvandor.numinoustreasury.droptable.NuminousDropTable;
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService;
import net.arvandor.numinoustreasury.jooq.Tables;
import net.arvandor.numinoustreasury.jooq.tables.records.NuminousNodeRecord;
import net.arvandor.numinoustreasury.profession.NuminousProfessionService;
import net.arvandor.numinoustreasury.NuminousTreasury;
import net.arvandor.numinoustreasury.jooq.tables.records.NuminousNodeRequiredProfessionRecord;
import net.arvandor.numinoustreasury.profession.NuminousProfession;
import org.bukkit.Location;
import org.bukkit.World;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class NuminousNodeRepository {

    private final NuminousTreasury plugin;
    private final DSLContext dsl;

    public NuminousNodeRepository(NuminousTreasury plugin, DSLContext dsl) {
        this.plugin = plugin;
        this.dsl = dsl;
    }

    public List<NuminousNode> getNodes() {
        return dsl.selectFrom(Tables.NUMINOUS_NODE)
                .fetch()
                .map(this::toDomain);
    }

    public void delete(String nodeId) {
        dsl.deleteFrom(Tables.NUMINOUS_NODE)
                .where(Tables.NUMINOUS_NODE.ID.eq(nodeId))
                .execute();
    }

    public void upsert(NuminousNode node) {
        dsl.insertInto(Tables.NUMINOUS_NODE)
                .set(Tables.NUMINOUS_NODE.ID, node.getId())
                .set(Tables.NUMINOUS_NODE.NAME, node.getName())
                .set(Tables.NUMINOUS_NODE.EXPERIENCE, node.getExperience())
                .set(Tables.NUMINOUS_NODE.STAMINA_COST, node.getStaminaCost())
                .set(Tables.NUMINOUS_NODE.DROP_TABLE_ID, node.getDropTable().getId())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WORLD_ID, node.getEntranceArea().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_X, node.getEntranceArea().getMinLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Y, node.getEntranceArea().getMinLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Z, node.getEntranceArea().getMinLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_X, node.getEntranceArea().getMaxLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Y, node.getEntranceArea().getMaxLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Z, node.getEntranceArea().getMaxLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_WORLD_ID, node.getEntranceWarpDestination().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_X, node.getEntranceWarpDestination().getX())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Y, node.getEntranceWarpDestination().getY())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Z, node.getEntranceWarpDestination().getZ())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_YAW, node.getEntranceWarpDestination().getYaw())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_PITCH, node.getEntranceWarpDestination().getPitch())
                .set(Tables.NUMINOUS_NODE.EXIT_WORLD_ID, node.getExitArea().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.EXIT_MIN_X, node.getExitArea().getMinLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.EXIT_MIN_Y, node.getExitArea().getMinLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.EXIT_MIN_Z, node.getExitArea().getMinLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.EXIT_MAX_X, node.getExitArea().getMaxLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.EXIT_MAX_Y, node.getExitArea().getMaxLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.EXIT_MAX_Z, node.getExitArea().getMaxLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_WORLD_ID, node.getExitWarpDestination().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_X, node.getExitWarpDestination().getX())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Y, node.getExitWarpDestination().getY())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Z, node.getExitWarpDestination().getZ())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_YAW, node.getExitWarpDestination().getYaw())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_PITCH, node.getExitWarpDestination().getPitch())
                .set(Tables.NUMINOUS_NODE.AREA_WORLD_ID, node.getArea().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.AREA_MIN_X, node.getArea().getMinLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.AREA_MIN_Y, node.getArea().getMinLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.AREA_MIN_Z, node.getArea().getMinLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.AREA_MAX_X, node.getArea().getMaxLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.AREA_MAX_Y, node.getArea().getMaxLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.AREA_MAX_Z, node.getArea().getMaxLocation().getBlockZ())
                .onConflict(Tables.NUMINOUS_NODE.ID).doUpdate()
                .set(Tables.NUMINOUS_NODE.NAME, node.getName())
                .set(Tables.NUMINOUS_NODE.EXPERIENCE, node.getExperience())
                .set(Tables.NUMINOUS_NODE.STAMINA_COST, node.getStaminaCost())
                .set(Tables.NUMINOUS_NODE.DROP_TABLE_ID, node.getDropTable().getId())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WORLD_ID, node.getEntranceArea().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_X, node.getEntranceArea().getMinLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Y, node.getEntranceArea().getMinLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Z, node.getEntranceArea().getMinLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_X, node.getEntranceArea().getMaxLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Y, node.getEntranceArea().getMaxLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Z, node.getEntranceArea().getMaxLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_WORLD_ID, node.getEntranceWarpDestination().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_X, node.getEntranceWarpDestination().getX())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Y, node.getEntranceWarpDestination().getY())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Z, node.getEntranceWarpDestination().getZ())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_YAW, node.getEntranceWarpDestination().getYaw())
                .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_PITCH, node.getEntranceWarpDestination().getPitch())
                .set(Tables.NUMINOUS_NODE.EXIT_WORLD_ID, node.getExitArea().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.EXIT_MIN_X, node.getExitArea().getMinLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.EXIT_MIN_Y, node.getExitArea().getMinLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.EXIT_MIN_Z, node.getExitArea().getMinLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.EXIT_MAX_X, node.getExitArea().getMaxLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.EXIT_MAX_Y, node.getExitArea().getMaxLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.EXIT_MAX_Z, node.getExitArea().getMaxLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_WORLD_ID, node.getExitWarpDestination().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_X, node.getExitWarpDestination().getX())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Y, node.getExitWarpDestination().getY())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Z, node.getExitWarpDestination().getZ())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_YAW, node.getExitWarpDestination().getYaw())
                .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_PITCH, node.getExitWarpDestination().getPitch())
                .set(Tables.NUMINOUS_NODE.AREA_WORLD_ID, node.getArea().getWorld().getUID().toString())
                .set(Tables.NUMINOUS_NODE.AREA_MIN_X, node.getArea().getMinLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.AREA_MIN_Y, node.getArea().getMinLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.AREA_MIN_Z, node.getArea().getMinLocation().getBlockZ())
                .set(Tables.NUMINOUS_NODE.AREA_MAX_X, node.getArea().getMaxLocation().getBlockX())
                .set(Tables.NUMINOUS_NODE.AREA_MAX_Y, node.getArea().getMaxLocation().getBlockY())
                .set(Tables.NUMINOUS_NODE.AREA_MAX_Z, node.getArea().getMaxLocation().getBlockZ())
                .where(Tables.NUMINOUS_NODE.ID.eq(node.getId()))
                .execute();
        dsl.deleteFrom(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION).where(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.NODE_ID.eq(node.getId())).execute();
        node.getApplicableProfessions().forEach(profession -> {
            dsl.insertInto(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION)
                    .set(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.NODE_ID, node.getId())
                    .set(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.PROFESSION_ID, profession.getId())
                    .set(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.LEVEL, node.getRequiredProfessionLevel(profession))
                    .execute();
        });
    }

    private NuminousNode toDomain(NuminousNodeRecord record) {
        NuminousDropTableService dropTableService = Services.INSTANCE.get(NuminousDropTableService.class);
        World entranceWorld = plugin.getServer().getWorld(UUID.fromString(record.getEntranceWorldId()));
        World entranceWarpDestinationWorld = plugin.getServer().getWorld(UUID.fromString(record.getEntranceWarpDestinationWorldId()));
        World exitWorld = plugin.getServer().getWorld(UUID.fromString(record.getExitWorldId()));
        World exitWarpDestinationWorld = plugin.getServer().getWorld(UUID.fromString(record.getExitWarpDestinationWorldId()));
        World areaWorld = plugin.getServer().getWorld(UUID.fromString(record.getAreaWorldId()));
        NuminousDropTable dropTable = dropTableService.getDropTableById(record.getDropTableId());
        NuminousProfessionService professionService = Services.INSTANCE.get(NuminousProfessionService.class);
        List<NuminousNodeRequiredProfessionRecord> requiredProfessionResults = dsl.selectFrom(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION).where(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.NODE_ID.eq(record.getId())).fetch();
        Map<NuminousProfession, Integer> requiredProfession = requiredProfessionResults.stream()
                .collect(Collectors.toMap(
                        result -> professionService.getProfessionById(result.getProfessionId()),
                        NuminousNodeRequiredProfessionRecord::getLevel
                ));
        return new NuminousNode(
                record.getId(),
                record.getName(),
                requiredProfession,
                record.getExperience(),
                record.getStaminaCost(),
                dropTable,
                new Area(
                        entranceWorld,
                        new Location(
                                entranceWorld,
                                record.getEntranceMinX(),
                                record.getEntranceMinY(),
                                record.getEntranceMinZ()
                        ),
                        new Location(
                                entranceWorld,
                                record.getEntranceMaxX(),
                                record.getEntranceMaxY(),
                                record.getEntranceMaxZ()
                        )
                ),
                new Location(
                        entranceWarpDestinationWorld,
                        record.getEntranceWarpDestinationX(),
                        record.getEntranceWarpDestinationY(),
                        record.getEntranceWarpDestinationZ(),
                        record.getEntranceWarpDestinationYaw(),
                        record.getEntranceWarpDestinationPitch()
                ),
                new Area(
                        exitWorld,
                        new Location(
                                exitWorld,
                                record.getExitMinX(),
                                record.getExitMinY(),
                                record.getExitMinZ()
                        ),
                        new Location(
                                exitWorld,
                                record.getExitMaxX(),
                                record.getExitMaxY(),
                                record.getExitMaxZ()
                        )
                ),
                new Location(
                        exitWarpDestinationWorld,
                        record.getExitWarpDestinationX(),
                        record.getExitWarpDestinationY(),
                        record.getExitWarpDestinationZ(),
                        record.getExitWarpDestinationYaw(),
                        record.getExitWarpDestinationPitch()
                ),
                new Area(
                        areaWorld,
                        new Location(
                                areaWorld,
                                record.getAreaMinX(),
                                record.getAreaMinY(),
                                record.getAreaMinZ()
                        ),
                        new Location(
                                areaWorld,
                                record.getAreaMaxX(),
                                record.getAreaMaxY(),
                                record.getAreaMaxZ()
                        )
                )
        );
    }

}
