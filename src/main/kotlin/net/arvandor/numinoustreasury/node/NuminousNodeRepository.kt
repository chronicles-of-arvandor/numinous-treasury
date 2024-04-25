package net.arvandor.numinoustreasury.node

import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.area.Area
import net.arvandor.numinoustreasury.droptable.NuminousDropTableService
import net.arvandor.numinoustreasury.jooq.Tables
import net.arvandor.numinoustreasury.jooq.tables.records.NuminousNodeRecord
import net.arvandor.numinoustreasury.jooq.tables.records.NuminousNodeRequiredProfessionRecord
import net.arvandor.numinoustreasury.profession.NuminousProfessionService
import org.bukkit.Location
import org.jooq.DSLContext
import java.util.UUID

class NuminousNodeRepository(private val plugin: NuminousTreasury, private val dsl: DSLContext) {
    val nodes: List<NuminousNode>
        get() =
            dsl.selectFrom(Tables.NUMINOUS_NODE)
                .fetch()
                .map { record: NuminousNodeRecord -> this.toDomain(record) }

    fun delete(nodeId: String?) {
        dsl.deleteFrom(Tables.NUMINOUS_NODE)
            .where(Tables.NUMINOUS_NODE.ID.eq(nodeId))
            .execute()
    }

    fun upsert(node: NuminousNode) {
        dsl.insertInto(Tables.NUMINOUS_NODE)
            .set(Tables.NUMINOUS_NODE.ID, node.id)
            .set(Tables.NUMINOUS_NODE.NAME, node.name)
            .set(Tables.NUMINOUS_NODE.EXPERIENCE, node.experience)
            .set(Tables.NUMINOUS_NODE.STAMINA_COST, node.staminaCost)
            .set(Tables.NUMINOUS_NODE.DROP_TABLE_ID, node.dropTable.id)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WORLD_ID, node.entranceArea.world.uid.toString())
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_X, node.entranceArea.minLocation.blockX)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Y, node.entranceArea.minLocation.blockY)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Z, node.entranceArea.minLocation.blockZ)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_X, node.entranceArea.maxLocation.blockX)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Y, node.entranceArea.maxLocation.blockY)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Z, node.entranceArea.maxLocation.blockZ)
            .set(
                Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_WORLD_ID,
                node.entranceWarpDestination.world!!.uid.toString(),
            )
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_X, node.entranceWarpDestination.x)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Y, node.entranceWarpDestination.y)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Z, node.entranceWarpDestination.z)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_YAW, node.entranceWarpDestination.yaw)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_PITCH, node.entranceWarpDestination.pitch)
            .set(Tables.NUMINOUS_NODE.EXIT_WORLD_ID, node.exitArea.world.uid.toString())
            .set(Tables.NUMINOUS_NODE.EXIT_MIN_X, node.exitArea.minLocation.blockX)
            .set(Tables.NUMINOUS_NODE.EXIT_MIN_Y, node.exitArea.minLocation.blockY)
            .set(Tables.NUMINOUS_NODE.EXIT_MIN_Z, node.exitArea.minLocation.blockZ)
            .set(Tables.NUMINOUS_NODE.EXIT_MAX_X, node.exitArea.maxLocation.blockX)
            .set(Tables.NUMINOUS_NODE.EXIT_MAX_Y, node.exitArea.maxLocation.blockY)
            .set(Tables.NUMINOUS_NODE.EXIT_MAX_Z, node.exitArea.maxLocation.blockZ)
            .set(
                Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_WORLD_ID,
                node.exitWarpDestination.world!!.uid.toString(),
            )
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_X, node.exitWarpDestination.x)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Y, node.exitWarpDestination.y)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Z, node.exitWarpDestination.z)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_YAW, node.exitWarpDestination.yaw)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_PITCH, node.exitWarpDestination.pitch)
            .set(Tables.NUMINOUS_NODE.AREA_WORLD_ID, node.area.world.uid.toString())
            .set(Tables.NUMINOUS_NODE.AREA_MIN_X, node.area.minLocation.blockX)
            .set(Tables.NUMINOUS_NODE.AREA_MIN_Y, node.area.minLocation.blockY)
            .set(Tables.NUMINOUS_NODE.AREA_MIN_Z, node.area.minLocation.blockZ)
            .set(Tables.NUMINOUS_NODE.AREA_MAX_X, node.area.maxLocation.blockX)
            .set(Tables.NUMINOUS_NODE.AREA_MAX_Y, node.area.maxLocation.blockY)
            .set(Tables.NUMINOUS_NODE.AREA_MAX_Z, node.area.maxLocation.blockZ)
            .onConflict(Tables.NUMINOUS_NODE.ID).doUpdate()
            .set(Tables.NUMINOUS_NODE.NAME, node.name)
            .set(Tables.NUMINOUS_NODE.EXPERIENCE, node.experience)
            .set(Tables.NUMINOUS_NODE.STAMINA_COST, node.staminaCost)
            .set(Tables.NUMINOUS_NODE.DROP_TABLE_ID, node.dropTable.id)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WORLD_ID, node.entranceArea.world.uid.toString())
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_X, node.entranceArea.minLocation.blockX)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Y, node.entranceArea.minLocation.blockY)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MIN_Z, node.entranceArea.minLocation.blockZ)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_X, node.entranceArea.maxLocation.blockX)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Y, node.entranceArea.maxLocation.blockY)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_MAX_Z, node.entranceArea.maxLocation.blockZ)
            .set(
                Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_WORLD_ID,
                node.entranceWarpDestination.world!!.uid.toString(),
            )
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_X, node.entranceWarpDestination.x)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Y, node.entranceWarpDestination.y)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_Z, node.entranceWarpDestination.z)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_YAW, node.entranceWarpDestination.yaw)
            .set(Tables.NUMINOUS_NODE.ENTRANCE_WARP_DESTINATION_PITCH, node.entranceWarpDestination.pitch)
            .set(Tables.NUMINOUS_NODE.EXIT_WORLD_ID, node.exitArea.world.uid.toString())
            .set(Tables.NUMINOUS_NODE.EXIT_MIN_X, node.exitArea.minLocation.blockX)
            .set(Tables.NUMINOUS_NODE.EXIT_MIN_Y, node.exitArea.minLocation.blockY)
            .set(Tables.NUMINOUS_NODE.EXIT_MIN_Z, node.exitArea.minLocation.blockZ)
            .set(Tables.NUMINOUS_NODE.EXIT_MAX_X, node.exitArea.maxLocation.blockX)
            .set(Tables.NUMINOUS_NODE.EXIT_MAX_Y, node.exitArea.maxLocation.blockY)
            .set(Tables.NUMINOUS_NODE.EXIT_MAX_Z, node.exitArea.maxLocation.blockZ)
            .set(
                Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_WORLD_ID,
                node.exitWarpDestination.world!!.uid.toString(),
            )
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_X, node.exitWarpDestination.x)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Y, node.exitWarpDestination.y)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_Z, node.exitWarpDestination.z)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_YAW, node.exitWarpDestination.yaw)
            .set(Tables.NUMINOUS_NODE.EXIT_WARP_DESTINATION_PITCH, node.exitWarpDestination.pitch)
            .set(Tables.NUMINOUS_NODE.AREA_WORLD_ID, node.area.world.uid.toString())
            .set(Tables.NUMINOUS_NODE.AREA_MIN_X, node.area.minLocation.blockX)
            .set(Tables.NUMINOUS_NODE.AREA_MIN_Y, node.area.minLocation.blockY)
            .set(Tables.NUMINOUS_NODE.AREA_MIN_Z, node.area.minLocation.blockZ)
            .set(Tables.NUMINOUS_NODE.AREA_MAX_X, node.area.maxLocation.blockX)
            .set(Tables.NUMINOUS_NODE.AREA_MAX_Y, node.area.maxLocation.blockY)
            .set(Tables.NUMINOUS_NODE.AREA_MAX_Z, node.area.maxLocation.blockZ)
            .where(Tables.NUMINOUS_NODE.ID.eq(node.id))
            .execute()
        dsl.deleteFrom(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION)
            .where(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.NODE_ID.eq(node.id)).execute()
        node.applicableProfessions.forEach { profession ->
            dsl.insertInto(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION)
                .set(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.NODE_ID, node.id)
                .set(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.PROFESSION_ID, profession.id)
                .set(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.LEVEL, node.getRequiredProfessionLevel(profession))
                .execute()
        }
    }

    private fun toDomain(record: NuminousNodeRecord): NuminousNode {
        val dropTableService =
            Services.INSTANCE.get(
                NuminousDropTableService::class.java,
            )
        val entranceWorld = plugin.server.getWorld(UUID.fromString(record.entranceWorldId))!!
        val entranceWarpDestinationWorld =
            plugin.server.getWorld(UUID.fromString(record.entranceWarpDestinationWorldId))
        val exitWorld = plugin.server.getWorld(UUID.fromString(record.exitWorldId))!!
        val exitWarpDestinationWorld = plugin.server.getWorld(UUID.fromString(record.exitWarpDestinationWorldId))
        val areaWorld = plugin.server.getWorld(UUID.fromString(record.areaWorldId))!!
        val dropTable = dropTableService.getDropTableById(record.dropTableId)!!
        val professionService =
            Services.INSTANCE.get(
                NuminousProfessionService::class.java,
            )
        val requiredProfessionResults: List<NuminousNodeRequiredProfessionRecord> =
            dsl.selectFrom(Tables.NUMINOUS_NODE_REQUIRED_PROFESSION).where(
                Tables.NUMINOUS_NODE_REQUIRED_PROFESSION.NODE_ID.eq(record.id),
            ).fetch()
        val requiredProfession =
            requiredProfessionResults.associate { result ->
                professionService.getProfessionById(result.professionId)!! to result.level
            }
        return NuminousNode(
            record.id,
            record.name,
            requiredProfession,
            record.experience,
            record.staminaCost,
            dropTable,
            Area(
                entranceWorld,
                Location(
                    entranceWorld,
                    record.entranceMinX.toDouble(),
                    record.entranceMinY.toDouble(),
                    record.entranceMinZ.toDouble(),
                ),
                Location(
                    entranceWorld,
                    record.entranceMaxX.toDouble(),
                    record.entranceMaxY.toDouble(),
                    record.entranceMaxZ.toDouble(),
                ),
            ),
            Location(
                entranceWarpDestinationWorld,
                record.entranceWarpDestinationX,
                record.entranceWarpDestinationY,
                record.entranceWarpDestinationZ,
                record.entranceWarpDestinationYaw,
                record.entranceWarpDestinationPitch,
            ),
            Area(
                exitWorld,
                Location(
                    exitWorld,
                    record.exitMinX.toDouble(),
                    record.exitMinY.toDouble(),
                    record.exitMinZ.toDouble(),
                ),
                Location(
                    exitWorld,
                    record.exitMaxX.toDouble(),
                    record.exitMaxY.toDouble(),
                    record.exitMaxZ.toDouble(),
                ),
            ),
            Location(
                exitWarpDestinationWorld,
                record.exitWarpDestinationX,
                record.exitWarpDestinationY,
                record.exitWarpDestinationZ,
                record.exitWarpDestinationYaw,
                record.exitWarpDestinationPitch,
            ),
            Area(
                areaWorld,
                Location(
                    areaWorld,
                    record.areaMinX.toDouble(),
                    record.areaMinY.toDouble(),
                    record.areaMinZ.toDouble(),
                ),
                Location(
                    areaWorld,
                    record.areaMaxX.toDouble(),
                    record.areaMaxY.toDouble(),
                    record.areaMaxZ.toDouble(),
                ),
            ),
        )
    }
}
